package datacentral.data.utils.concurent

import datacentral.data.utils.concurent.Composable.{FutureTry, Status}
import datacentral.data.utils.concurent.ResumableTask.KeyManager
import datacentral.data.utils.concurent.Task.FutureTried
import datacentral.data.utils.extensions.TryExtensions.InformativeRuntimeError

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext}

/**
  * Each instance of this class `Task` represents a concurrent computation that returns any data types,
  * including `UNIT`, although, for the case of a `UNIT`-return we may consider using `ResumableTask` instead.
  *
  * By registering dependencies against instances of this class, we can build up arbitrary DAG while making sure that:
  * - each node is executed only once (if necessary, and can be skipped otherwise)
  * - each computation result can be shared freely across the entire graph
  * - user do not need to have a complete description of the entire graph while declaring any node
  * - dependencies can be registered using `dependsOn`, `dependedBy` methods or flatMap/for-comprehension
  *
  * Each instance of this is required to implement these items:
  * - compute: A
  *
  * * @tparam A is the type of output value from `compute`
  */
abstract class Task[A](implicit executionContext: ExecutionContext) extends Composable {

  /**
    * A call to this value `result` will trigger `completion` on its dependencies (and subsequently trigger further
    * `result`s for all tasks leading up to this current task) and then `compute: A`. Each task is executed in a
    * separate non-blocking `Future`
    */
  lazy val result: FutureTry[A] = {
    started = true // mark this task as `started` so that no more dependencies can be registered against it
    resolveDepsAndExecuteWithTimer(compute)
  }
  /**
    * Both `result` and `completion` are lazy, hence calling either of them will trigger the task to `compute: A`. We
    * can call `result` immediately after a task is defined, which forbids any further dependency declaration on the
    * task.
    *
    * Alternatively, calling `result` on the final task only will trigger the entire DAG leading up to the final task,
    * which gives more flexibility in term of binding dependencies in the up-stream of the DAG.
    *
    * The second approach is more advantageous since unnecessary tasks that are not up-stream to the final task will
    * not be result. Hence, one can construct many final tasks based on a shared collection of tasks and chose to
    * trigger the one necessary in different situations. In combination with using conditional-binding flag `when`, we
    * can turn features on and off easily.
    */
  override lazy val completion: Status = result.mapResult { _ => Unit }

  /**
    * Registering a follow-up computation of the current task to return a new task, which can be used independently
    * from the current task. However, the computation in this current task won't be duplicated, but reused, in the new
    * task.
    *
    * More importantly, this method ensure the dependencies of the new task are automatically wired. Alternatively, if
    * `this.result: FutureTry[A]` is consumed using `get` while constructing new tasks, then separate calls to
    * `dependsOn` need to be logged against this task otherwise dependencies resolution for the newly created tasks
    * may be misleading, i.e. dead-lock condition may go undetected
    *
    * @param transform is the follow-up action
    * @tparam B is the type of the new output
    * @return a new `Task` with `compute(routine)` as the new computation
    */
  def map[B](transform: A => B): Task[B] = Task[B](
    this.getThenMap(transform)
  ).dependsOn(this)

  def getThenMap[B](transform: A => B): B = result.mapAndGet(transform)

  /**
    * Same with the other method but return a `ResumableTask`
    *
    * @param key     is a unique string identifier used for persisting the status of this task computation to disk
    * @param routine is the follow-up action
    * @return a `Resumable` task
    */
  def mapWith(key: String, routine: A => Unit)(implicit keyManager: KeyManager): ResumableTask = ResumableTask(key,
    this.getThenMap(routine)
  ).dependsOn(this)

  def flatMap[B](tranform: A => Task[B]): Task[B] = Task[B](
    tranform(this.get).get
  ).dependsOn(this)

  // some utils functions including those for the Monad concept.
  def get: A = result.get

  /**
    * This is made abstract to widen the use-cases of `Task`. A default use-case is is described in the `apply` method
    * of its companion object. Other use-cases fall in the situation when we simply extend this class and provide a
    * custom-built `compute` from our own class constructor parameters
    */
  protected def compute: A
}

object Task {

  /**
    * Gives the default implementation for `Task` which requires
    *
    * @param out a by-name parameter
    * @param ec  future Execution Context
    * @tparam A output type can be inferred
    * @return a new `Task` that wrap around the computation for `out: A`
    */
  def apply[A](out: => A)(implicit ec: ExecutionContext): Task[A] = new DefaultImpl[A](out)(ec)

  /**
    * This is another use-case which allow us to perform a while-loop conveniently
    *
    * @param check   is the boolean condition for the while-loop
    * @param repeat  is the main computation perform in each iteration
    * @param maxIter is maximum number of iterations
    * @param ec
    * @tparam A
    */
  class TaskRepeatable[A](check: => Boolean, repeat: => A, maxIter: Int = 1)(implicit val ec: ExecutionContext)
    extends Task[A] {
    private var count: Int = 0
    private var out: A = _

    override protected def compute: A = {
      while (check && count < maxIter) {
        count += 1
        out = repeat
      }
      out
    }
  }

  /**
    * This is the default implementation for `Task`
    *
    * @param out is the required computation to perform by this task
    * @param ec  implicit execution context for Scala Future
    * @tparam A is any output Type
    */
  private class DefaultImpl[A](out: => A)(implicit val ec: ExecutionContext) extends Task[A] {
    override protected def compute: A = out
  }

  /**
    * Contains convenient helpers for manipulating `Task`'s output
    *
    * @param out is the given output
    * @tparam A is type of `routine`
    */
  implicit class FutureTried[A](out: FutureTry[A])(implicit ec: ExecutionContext) {

    /**
      * This provide the monad transformer for `Future[Try[_]]`
      *
      * @param transform
      * @tparam B
      * @return
      */
    def flatMap[B](transform: A => FutureTried[B]): FutureTried[B] = out.mapResult((a: A)
    => transform(a).get)

    /**
      * Chaining `compute` to the core result of `routine`
      */
    def mapResult[B](transform: A => B): FutureTry[B] = out.map(_.map(transform))

    /**
      * Retrieve the result of `routine`
      *
      * @param duration is the waiting period for getting the result of routine, which is defaulted to Inf. time.
      * @return the result of `routine`, which can be an exception
      */
    def get(implicit duration: Duration = Duration.Inf): A = Await.result(out, duration).recover({
      case e: Throwable => throw InformativeRuntimeError(e)
    }).get

    /**
      * Apply further computation on the inner output of `compute`. Since the required computation can only commence
      * when `compute` has finished, using `get` and therefore `Await.result()` is acceptable here
      *
      * @param transform is the computation required
      * @param duration  is the waiting period for getting the result of routine, which is defaulted to Inf. time.
      * @tparam B is the output type of `compute`
      * @return an instance of `B`, which can be an exception
      */
    def mapAndGet[B](transform: A => B)(implicit duration: Duration = Duration.Inf): B = transform(get)
  }

}