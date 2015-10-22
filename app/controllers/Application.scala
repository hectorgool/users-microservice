package controllers


import com.twitter.bijection.twitter_util._
import com.twitter.io.Charsets
import lib._
import org.jboss.netty.util.CharsetUtil
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.{Json,JsValue}
import play.api.libs.json.Json.{toJson}
import play.api.mvc._
import play.api.Play.current
import play.api._
import scala.concurrent.{Future}
import scala.util.{Failure, Success}


class Application extends Controller with UtilBijections {


	val elasticsearchIndex = current.configuration.getString("elasticsearch.index").get
	val indexType = current.configuration.getString("elasticsearch.indexType").get
	val size = current.configuration.getString("elasticsearch.size").get

  	def index = Action {
    	Ok(views.html.index("Your API is ready."))
  	}	

	def createDocument = Action.async(parse.json) { request =>
 		
		val reqData: JsValue = request.body

		println("reqData: " + reqData)

		val id = (reqData \ "id").as[String]
		val firstname = (reqData \ "firstname").as[String]
		val lastname  = (reqData \ "lastname").as[String]
		val email     = (reqData \ "email").as[String]
		val username  = (reqData \ "username").as[String]

		val json: JsValue = Json.obj(
      		"firstname" -> firstname,
      		"lastname"  -> lastname,
      		"email"     -> email,
      		"username"  -> username
		)

		val futureScala = twitter2ScalaFuture.apply( FinagleClient.documentSave( List( elasticsearchIndex, indexType, id.toString  ), json ) )
        
		futureScala.map( f => 
			Ok( Json.parse( f.getContent.toString( CharsetUtil.UTF_8 ) ) )
		)

	}

	def readDocument = Action.async(parse.json) { request =>
 
		val term = (request.body \ "term").as[String]
		
		val json: JsValue = Json.obj(
			"size" -> size,
			"query" -> Json.obj(
				"match" -> Json.obj(
					"_all" -> Json.obj(
						"query" -> term,
                		"operator" -> "and"	
					)
				)
			),
			"sort" -> Json.arr(
				Json.obj(
					"lastname" -> Json.obj( "order"-> "asc", "mode" -> "avg")
				)
			)
		)

		val futureScala = twitter2ScalaFuture.apply( FinagleClient.documentSearch( elasticsearchIndex, indexType, json ) )
        
		futureScala.map( f => 
			Ok( Json.parse( f.getContent.toString( CharsetUtil.UTF_8 ) ) )
		)

	}	
	
	def updateDocument = Action.async(parse.json) { request =>
 		
		val reqData: JsValue = request.body

		val id = (reqData \ "id").as[String]
		val firstname = (reqData \ "firstname").as[String]
		val lastname  = (reqData \ "lastname").as[String]
		val email     = (reqData \ "email").as[String]
		val username  = (reqData \ "username").as[String]

		val json: JsValue = Json.obj(
			"doc" -> Json.obj(
	      		"firstname" -> firstname,
	      		"lastname"  -> lastname,
	      		"email"     -> email,
	      		"username"  -> username
  			),
  			"doc_as_upsert"-> "true"
		)

		val futureScala = twitter2ScalaFuture.apply( FinagleClient.documentSave( List( elasticsearchIndex, indexType, id, "_update" ), json ) )
        
		futureScala.map( f => 
			Ok( Json.parse( f.getContent.toString( CharsetUtil.UTF_8 ) ) )
		)

	}

	def deleteDocument( id: String ) = Action.async {

		val futureScala = twitter2ScalaFuture.apply( FinagleClient.documentDelete( List( elasticsearchIndex, indexType, id ) ) )
        
		futureScala.map( f => 
			Ok( Json.parse( f.getContent.toString( CharsetUtil.UTF_8 ) ) )
		)

	}


}
