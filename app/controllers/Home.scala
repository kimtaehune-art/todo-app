/**
 *
 * to do sample project
 *
 */

package controllers

import lib.persistence.UserRepository

import javax.inject._
import play.api.mvc._

import model.ViewValueHome

@Singleton
class HomeController @Inject() (
  val controllerComponents: ControllerComponents,
  userRepository: UserRepository, // Sample: Delete this line
  // ※ Repository を DI するには modules.DatabaseModule の実装が必要です
) extends BaseController {

  def index() = Action { implicit req =>
    val vv = ViewValueHome(
      title  = "Home",
      cssSrc = Seq("main.css"),
      jsSrc  = Seq("main.js")
    )
    Ok(views.html.Home(vv))
  }
}
