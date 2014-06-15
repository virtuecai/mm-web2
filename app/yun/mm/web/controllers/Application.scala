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

  import yun.mm.web.controllers.user.Users.UserWrites

  val logger = play.api.Logger("web2.1")

  def index(path: String) = Action {
    implicit request =>
      Ok(html.web.application.index())
  }

  val loginReads = (
    (__ \ 'username).read[String](maxLength[String](30)) and
      (__ \ 'password).read[String](maxLength[String](30)) and
      (__ \ 'remember).readNullable[Boolean]
    ).tupled

  def authenticate = Action(parse.json) {
    implicit request =>
      request.body.validate(loginReads).fold(errors => BadRequest(yun.mm.common.util.FormUtils.toFlatJson(errors)), {
        case (username, password, rememberOpt) =>
          models.user.User.list('name -> username, 'password -> password).headOption.map {
            x =>
              val r = Ok(Json.toJson(x)).withSession(Security.username -> x.nicknameOpt.get, SESSION_USER_ID -> x.id.toString)
              rememberOpt.filter(_ == true).map(m =>
                r.withCookies(Cookie(COOKIE_USERNAME, java.net.URLEncoder.encode(username, "utf-8")), Cookie(COOKIE_PASSWORD, java.net.URLEncoder.encode(password, "utf-8")))
              ).getOrElse(r.discardingCookies(DiscardingCookie(COOKIE_USERNAME), DiscardingCookie(COOKIE_PASSWORD)))
          }.getOrElse(NotFound)
      })
  }

  def logout = Action {
    Ok.withNewSession.flashing()
  }

  def currentUser = Action {
    implicit request =>
      request.session.get(SESSION_USER_ID).flatMap(userId => models.user.User.findById(userId.toLong)).map(
        x => Ok(Json.toJson(x))
      ).getOrElse(NotFound)
  }

}
