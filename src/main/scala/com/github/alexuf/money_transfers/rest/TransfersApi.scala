package com.github.alexuf.money_transfers.rest

import com.github.alexuf.money_transfers.model.Transfer
import com.github.alexuf.money_transfers.rest.RestApi.JsonResponse
import com.github.alexuf.money_transfers.service.TransfersStorageComponent
import com.twitter.finagle.Service
import com.twitter.finagle.http.path.{/, Long, Path, Root}
import com.twitter.finagle.http.{Method, Request}
import com.twitter.util.Future

/**
  * Created by alexuf on 13/03/2017.
  */

class TransfersApi(transfers: TransfersStorageComponent#TransfersStorage) {

  import TransfersApi._

  def get(id: Long)(request: Request): Future[TransferResponse] = Future.value {
    transfers.get(id)
      .getOrElse(throw new NoSuchElementException())
      .toResponse
  }

  def put()(request: Request): Future[TransferResponse] = Future.value {
    val createTransferRequest = RestApi.readJsonValue(request.contentString, classOf[CreateTransferRequest])
    transfers.create(createTransferRequest.src, createTransferRequest.dst, createTransferRequest.amount)
      .toResponse
  }

  def apply(method: Method, path: Path): Service[Request, JsonResponse] = (method, path) match {
    case (Method.Get, Root / Long(id)) => Service.mk(get(id))
    case (Method.Put, Root) => Service.mk(put())
  }
}

object TransfersApi {

  case class CreateTransferRequest(src: Long, dst: Long, amount: Long)

  case class TransferResponse(id: Long, amount: Long, state: String) extends JsonResponse

  implicit class TransferResponseDecorator(val transfer: Transfer) extends AnyVal {
    def toResponse = TransferResponse(transfer.id, transfer.amount, transfer.state.name)
  }

}
