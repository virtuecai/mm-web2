package yun.mm.web.controllers

import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._
import play.api.mvc._
import views._
import yun.mm.models

/**
 * @author caizhengda
 * @version 0.1, 2014/06/13
 */
object Application extends Controller with Secured {

  val logger = play.api.Logger("web2.1")

  implicit val UserWrites = new Writes[models.user.User] {
    def writes(o: models.user.User): JsValue = {
      Json.obj(
        "id" -> o.id.get,
        "name" -> o.name,
        "nicknameOpt" -> o.nicknameOpt
      )
    }
  }

  def index(path: String) = Action {
    implicit request =>
      Ok(html.web.application.index())
  }

  val loginReads = (
    (__ \ 'username).read[String](maxLength[String](30)) and
      (__ \ 'password).read[String](maxLength[String](30)) and
      (__ \ 'remember).readNullable[String]
    ).tupled

  def authenticate = Action(parse.json) {
    implicit request =>
      request.body.validate(loginReads).fold(errors => BadRequest(yun.mm.common.util.FormUtils.toFlatJson(errors)), {
        case (username, password, rememberOpt) =>
          models.user.User.list('name -> username, 'password -> password).headOption.map(
            x => Ok(Json.toJson(x)).withSession(Security.username -> x.nicknameOpt.get, USER_ID -> x.id.toString).withCookies(Cookie("username", java.net.URLEncoder.encode(username, "utf-8")), Cookie("password", java.net.URLEncoder.encode(password, "utf-8")))
          ).getOrElse(NotFound)
      })
  }

  def logout = Action {
    Ok.withNewSession.flashing()
  }

  def currentUser = Action {
    implicit request =>
      request.session.get(USER_ID).flatMap(userId => models.user.User.findById(userId.toLong)).map(
        x => Ok(Json.toJson(x))
      ).getOrElse(NotFound)
  }

}
