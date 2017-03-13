package com.github.alexuf.money_transfers.service

import java.util.concurrent.Executors

import com.github.alexuf.money_transfers.model.Transfer
import com.google.common.util.concurrent.ThreadFactoryBuilder

/**
  * Created by alexuf on 13/03/2017.
  */
trait TransfersProcessorComponent {
  self: AccountsStorageComponent with TransfersStorageComponent =>

  trait TransfersProcessor {

    final protected def process(transfer: Transfer): Unit = {
      val src = accounts.get(transfer.src.id).get
      val dst = accounts.get(transfer.dst.id).get
      if (src.limit.forall(transfer.amount - src.balance <= _)) {
        accounts.incrementBalance(src.id, -transfer.amount)
        accounts.incrementBalance(dst.id, +transfer.amount)
        transfers.updateState(transfer.id, Transfer.State.Completed)
      } else {
        transfers.updateState(transfer.id, Transfer.State.Rejected)
      }
    }

    def schedule(transfer: Transfer): Unit

    def close(): Unit
  }

  def transferProcessor: TransfersProcessor

  class SerialTransferProcessor extends TransfersProcessor {

    private val executor = Executors.newSingleThreadExecutor(
      new ThreadFactoryBuilder().setNameFormat("transfer-processor-thread").build()
    )

    override def close(): Unit = {
      executor.shutdown()
    }

    override def schedule(transfer: Transfer): Unit = executor.execute { () => process(transfer) }
  }

}