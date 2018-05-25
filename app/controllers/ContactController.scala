package controllers

import javax.inject.Inject
import models.Contact
import play.api.db.Database
import play.api.mvc.InjectedController
import services.{Secured, contactService, registerService}


class ContactController @Inject()(db: Database, contactService:contactService, rs:registerService, auth:AuthController) extends InjectedController with Secured with play.api.i18n.I18nSupport {

  def index = withAuth { username => implicit request =>
    Ok(views.html.index())
  }

  def view = withAuth { username  => implicit request =>
    val size = contactService.all.size
    Ok(views.html.viewContacts(contactService.all, "", size))
  }

  def createPage = withAuth { username => implicit request =>
    Ok(views.html.create(contactService.contactForm))
  }


  def createContact = withAuth { username =>  implicit request =>
    contactService.contactForm.bindFromRequest.fold(
      error => {
        BadRequest(views.html.create(error))
      },
      success => {
        contactService.create(success)
        val username = success.name
        Redirect(routes.ContactController.view()).flashing("success" -> s"Contact saved: $username")
      }
    )
  }

  def edit(id:Long) = withAuth { username =>  implicit request =>
    try {
      val getID: Contact = contactService.get(id)
      Ok(views.html.edit(getID.id, contactService.contactForm.fill(getID)))
    }
    catch {
      case e:Exception => Redirect(routes.ContactController.view)
    }
  }


  def update(id: Long) = withAuth { username => implicit request =>
    val getID = contactService.get(id)
    contactService.contactForm.bindFromRequest.fold(
      errors => BadRequest(views.html.edit(id, errors)),
      success => {
        contactService.update(id,success)
        val username = success.name
        Redirect(routes.ContactController.view).flashing("success" -> s"Updated: $username")
      }
    )
  }

  def delete(id: Long) = Action {
    val user = contactService.get(id)
    val username = user.name
    contactService.delete(id)
    Redirect(routes.ContactController.view).flashing("success" -> s"Contact deleted: $username")
  }

  def search(search: String) = withAuth { username => implicit request =>
    val searchResult = request.body.asFormUrlEncoded.get("search")(0)
    val filteredList:List[Contact] = contactService.seachByName(searchResult)
    val size = filteredList.size
    Ok(views.html.viewContacts(filteredList, search, size))
  }

}
