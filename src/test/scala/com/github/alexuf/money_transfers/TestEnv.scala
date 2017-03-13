package com.github.alexuf.money_transfers

import com.github.alexuf.money_transfers.model.Transfer
import com.github.alexuf.money_transfers.service.{AccountsStorageComponent, TransfersProcessorComponent, TransfersStorageComponent}
import org.scalatest.mockito.MockitoSugar

/**
  * Created by alexuf on 13/03/2017.
  */
trait TestEnv extends AccountsStorageComponent with TransfersStorageComponent with TransfersProcessorComponent with MockitoSugar {

  class ImmediateTransfersProcessor extends TransfersProcessor {

    override def schedule(transfer: Transfer): Unit = process(transfer)

    override def close(): Unit = ()
  }

  override val accounts: AccountsStorage = new InMemAccountsStorage
  override val transfers: TransfersStorage = new InMemTransfersStorage
  override val transferProcessor: TransfersProcessor = mock[TransfersProcessor]
}
