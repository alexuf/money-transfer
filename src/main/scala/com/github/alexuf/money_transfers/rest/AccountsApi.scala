package com.github.alexuf.money_transfers.rest

import com.github.alexuf.money_transfers.model.Account
import com.github.alexuf.money_transfers.rest.RestApi.JsonResponse
import com.github.alexuf.money_transfers.service.AccountsStorageComponent
import com.twitter.finagle.Service
import com.twitter.finagle.http.path.{/, Long, Path, Root}
import com.twitter.finagle.http.{Method, Request}
import com.twitter.util.Future

/**
  * Created by alexuf on 13/03/2017.
  */

class AccountsApi(accounts: AccountsStorageComponent#AccountsStorage) {

  import AccountsApi._

  def get(id: Long)(request: Request): Future[AccountResponse] = Future.value {
    accounts.get(id)
      .getOrElse(throw new NoSuchElementException())
      .toResponse
  }

  def put()(request: Request): Future[AccountResponse] = Future.value {
    val createAccountRequest = RestApi.readJsonValue(request.contentString, classOf[CreateAccountRequest])
    accounts.create(createAccountRequest.limit).toResponse
  }

  def apply(method: Method, path: Path): Service[Request, JsonResponse] = (method, path) match {
    case (Method.Get, Root / Long(id)) => Service.mk(get(id))
    case (Method.Put, Root) => Service.mk(put())
  }
}

object AccountsApi {

  case class CreateAccountRequest(limit: Option[Long])

  case class AccountResponse(id: Long, balance: Long, limit: Option[Long]) extends JsonResponse

  implicit class AccountResponseDecorator(val account: Account) extends AnyVal {
    def toResponse = AccountResponse(account.id, account.balance, account.limit)
  }

}