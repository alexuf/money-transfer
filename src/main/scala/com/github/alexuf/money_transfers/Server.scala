package com.github.alexuf.money_transfers

import com.github.alexuf.money_transfers.rest.RestApi
import com.github.alexuf.money_transfers.service.{AccountsStorageComponent, TransfersProcessorComponent, TransfersStorageComponent}
import com.twitter.util.Await


object Server extends App with AccountsStorageComponent with TransfersStorageComponent with TransfersProcessorComponent with RestApi {

  override val accounts = new InMemAccountsStorage
  override val transfers = new InMemTransfersStorage
  override val transferProcessor = new SerialTransferProcessor

  sys.addShutdownHook {
    server.close()
    transferProcessor.close()
  }

  Await.ready(server)
}