package yun.mm.web.controllers

import play.api.mvc._
import views._

/**
 * @author caizhengda
 * @version 0.1, 2014/06/13
 */
object Application extends Controller  {

  val logger = play.api.Logger("web2.0")

  def index(path: String) = Action {
    implicit request =>
     Ok(html.web.application.index())
  }

}
