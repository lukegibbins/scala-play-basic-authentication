package services
import javax.inject.Inject
import models.Contact
import anorm.{RowParser, _}
import play.api.data.Form
import play.api.data.Forms._
import play.api.db.Database
import com.google.inject.ImplementedBy


class RealContactService @Inject()(db: Database) extends contactService {

  val parser:RowParser[Contact] = Macro.namedParser[Contact]

  override def all: List[Contact] = {
    db.withConnection { implicit connection =>
      SQL("SELECT id, name, email FROM contacts").as(parser.*)
    }
  }

  override def create(contact: Contact) = {
    db.withConnection { implicit connection =>
      SQL("INSERT INTO contacts (name, email) VALUES ({name}, {email})").on(
        "name"-> contact.name,
        "email"-> contact.email).executeInsert()
    }
  }

  override def get(id:Long):Contact = {
    db.withConnection { implicit connection =>
      SQL("SELECT id, name, email FROM contacts where id = {id}").on("id" -> id).as(parser.single)
    }
  }


  override def update(id:Long, contact:Contact) = {
    db.withConnection { implicit connection =>
      SQL("UPDATE contacts SET name = {name}, email = {email} WHERE id = {id}").on(
        "id"-> id,
        "name"-> contact.name,
        "email"-> contact.email
      ).executeUpdate()
    }
  }


  override def delete(id:Long) = {
    db.withConnection { implicit connection =>
      SQL("DELETE FROM contacts WHERE id = {id}").on("id" -> id).executeUpdate()
    }
  }

  override def seachByName(search:String): List[Contact] = {
    db.withConnection{ implicit connection =>
      SQL("SELECT id, name, email FROM contacts WHERE name LIKE {name} OR lower(name) LIKE {name}").on("name" -> s"%$search%").as(parser.*)
    }
  }

  override def contactForm = Form(
    mapping(
      "id" -> ignored(0L),
      "name"-> nonEmptyText,
      "email"-> email
    )(Contact.apply)(Contact.unapply)
  )
}


@ImplementedBy(classOf[RealContactService])
trait contactService {
  def all:List[Contact]
  def create(contact: Contact)
  def get(id:Long): Contact
  def update(id:Long, contact:Contact)
  def delete(id:Long)
  def contactForm : Form[Contact]
  def seachByName(search:String):List[Contact]
}

