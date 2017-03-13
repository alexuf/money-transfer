package com.github.alexuf.money_transfers.rest

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.github.alexuf.money_transfers.service.{AccountsStorageComponent, TransfersStorageComponent}
import com.twitter.finagle.http._
import com.twitter.finagle.http.path./:
import com.twitter.finagle.http.service.RoutingService
import com.twitter.finagle.{Filter, Http, ListeningServer, Service}
import com.twitter.io.Bufs
import com.twitter.util.{Future, Return, Throw}

/**
  * Created by alexuf on 12/03/2017.
  */
trait RestApi {
  self: AccountsStorageComponent with TransfersStorageComponent =>

  import RestApi._

  private lazy val accountsApi = new AccountsApi(accounts)
  private lazy val transfersApi = new TransfersApi(transfers)

  protected val service: Service[Request, Response] = RoutingService.byMethodAndPathObject[Request] {
    case (method, "accounts" /: path) => jsonFilter andThen accountsApi(method, path)
    case (method, "transfers" /: path) => jsonFilter andThen transfersApi(method, path)
  }

  val server: ListeningServer = Http.serve(":8080", service)
}

object RestApi {

  val mapper: ObjectMapper = {
    val mapper = new ObjectMapper()
    mapper.registerModule(DefaultScalaModule)
    mapper
  }

  def createJsonResponse(status: Status, result: (String, AnyRef)): Response = {
    val response = Response(status)
    val writer = mapper.writer()
    response.content = Bufs.utf8Buf(writer.writeValueAsString(Map(result)))
    response.mediaType = MediaType.Json
    response
  }

  trait JsonResponse

  def jsonFilter[Rep <: JsonResponse]: Filter[Request, Response, Request, Rep] = Filter.mk { case (request, continue) =>
    continue(request).transform { response =>

      val (status, content) = response match {
        case Return(result) =>
          Status.Ok -> ("result" -> result)
        case Throw(ex) =>
          val status = ex match {
            case _: IllegalArgumentException => Status.BadRequest
            case _: NoSuchElementException => Status.NotFound
            case _ => Status.InternalServerError
          }
          status -> ("error" -> ex.getMessage)
      }

      Future.value(createJsonResponse(status, content))
    }
  }

  def readJsonValue[T](jsonString: String, clz: Class[T]): T = {
    val reader = RestApi.mapper.readerFor(clz)
    reader.readValue[T](jsonString)
  }
}