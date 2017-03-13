package com.github.alexuf.money_transfers.service

import java.util.ConcurrentModificationException
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

import com.github.alexuf.money_transfers.model.Transfer

/**
  * Created by alexuf on 12/03/2017.
  */
trait TransfersStorageComponent {
  self: AccountsStorageComponent with TransfersProcessorComponent =>

  trait TransfersStorage {
    def get(id: Long): Option[Transfer]

    def create(src: Long, dst: Long, amount: Long): Transfer

    def updateState(id: Long, state: Transfer.State): Transfer
  }

  def transfers: TransfersStorage

  class InMemTransfersStorage extends TransfersStorage {

    private val idSeq = new AtomicLong()

    private val storage = new ConcurrentHashMap[Long, Transfer]()

    override def get(id: Long): Option[Transfer] = {
      Option(storage.get(id))
    }

    override def create(src: Long, dst: Long, amount: Long): Transfer = {
      val id = idSeq.incrementAndGet()
      val srcAccount = accounts.get(src)
        .getOrElse(throw new IllegalArgumentException(s"src account $src not found"))
      val dstAccount = accounts.get(dst)
        .getOrElse(throw new IllegalArgumentException(s"dst account $dst not found"))
      val transfer = Transfer(
        id = id,
        src = srcAccount,
        dst = dstAccount,
        amount = amount,
        state = Transfer.State.Pending
      )
      storage.put(id, transfer)
      transferProcessor.schedule(transfer)
      transfer
    }

    override def updateState(id: Long, state: Transfer.State): Transfer = {
      val transfer = storage.get(id)

      if (transfer.state != Transfer.State.Pending)
        throw new IllegalStateException("could't change final state")

      val updated = transfer.copy(state = state)

      if (!storage.replace(id, transfer, updated))
        throw new ConcurrentModificationException("state already changed")

      updated
    }
  }

}