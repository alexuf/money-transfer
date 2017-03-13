package com.github.alexuf.money_transfers.rest

import com.github.alexuf.money_transfers.TestEnv
import com.github.alexuf.money_transfers.model.Transfer
import com.github.alexuf.money_transfers.rest.AccountsApi.{AccountResponse, CreateAccountRequest}
import com.github.alexuf.money_transfers.rest.TransfersApi.{CreateTransferRequest, TransferResponse}
import com.twitter.finagle.http.{RequestBuilder, Response}
import com.twitter.io.Bufs
import com.twitter.util.Await
import org.scalatest.FlatSpec

/**
  * Created by alexuf on 13/03/2017.
  */
class RestApiSpec extends FlatSpec with RestApi with TestEnv {

  override val transferProcessor: TransfersProcessor = new ImmediateTransfersProcessor

  it should "create accounts" in {
    val account = {
      val request = RequestBuilder().url("http://localhost/accounts").buildPut(Bufs.utf8Buf(
        RestApi.mapper.writer().writeValueAsString(CreateAccountRequest(limit = None))
      ))

      val response = Await.result(service(request))

      readResponse(response, classOf[AccountResponse])
    }

    assertResult(None) {
      account.limit
    }

    assertResult(account) {

      val request = RequestBuilder().url(s"http://localhost/accounts/${account.id}").buildGet()

      val response = Await.result(service(request))

      readResponse(response, classOf[AccountResponse])
    }
  }

  it should "create transfers" in {

    val src = accounts.create(None)
    val dst = accounts.create(None)

    val transfer = {
      val request = RequestBuilder().url("http://localhost/transfers").buildPut(Bufs.utf8Buf(
        RestApi.mapper.writer().writeValueAsString(CreateTransferRequest(src.id, dst.id, 42))
      ))

      val response = Await.result(service(request))

      readResponse(response, classOf[TransferResponse])
    }

    assertResult(Transfer.State.Pending.name) {
      transfer.state
    }

    assertResult(transfer.copy(state = Transfer.State.Completed.name)) {

      val request = RequestBuilder().url(s"http://localhost/transfers/${transfer.id}").buildGet()

      val response = Await.result(service(request))

      readResponse(response, classOf[TransferResponse])
    }
  }

  private def readResponse[T](response: Response, clz: Class[T]) = {
    val reader = RestApi.mapper.readerFor(clz)
    val json = reader.readTree(response.contentString)

    if (json.has("error")) {
      throw new RuntimeException(json.get("error").asText())
    }

    reader.treeToValue(json.get("result"), clz)
  }
}