package controllers

import javax.inject._

import common.ControllerWriters
import play.api.mvc._

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class Home @Inject() extends ControllerWriters with Controller {

  /**
   * Create an Action to render an HTML page with a welcome message.
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */
  def index = Action {
    Ok(scalatags.htmllib.index("Welcome you to Data Central"))
  }

}
