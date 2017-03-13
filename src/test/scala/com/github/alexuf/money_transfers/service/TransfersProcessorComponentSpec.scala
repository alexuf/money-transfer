package com.github.alexuf.money_transfers.service

import com.github.alexuf.money_transfers.TestEnv
import com.github.alexuf.money_transfers.model.Transfer
import org.scalatest.FlatSpec

/**
  * Created by alexuf on 13/03/2017.
  */
class TransfersProcessorComponentSpec extends FlatSpec with TestEnv {

  class ImmediateTransfersProcessor extends TransfersProcessor {

    override def schedule(transfer: Transfer): Unit = process(transfer)

    override def close(): Unit = ()
  }

  override val transferProcessor: TransfersProcessor = new ImmediateTransfersProcessor

  it should "complete transfers for unlimited accounts" in {
    val src = accounts.create(None)
    val dst = accounts.create(None)
    val t = transfers.create(src.id, dst.id, src.balance + 1)

    assertResult(Transfer.State.Pending) {
      t.state
    }

    assertResult(Some(t.copy(state = Transfer.State.Completed))) {
      transfers.get(t.id)
    }

    assertResult(dst.balance + t.amount) {
      accounts.get(dst.id).get.balance
    }

    assertResult(src.balance - t.amount) {
      accounts.get(src.id).get.balance
    }

  }

  it should "complete transfers if balance + limit >= amount" in {
    val src = accounts.incrementBalance(
      accounts.create(Some(42)).id,
      42
    )

    val dst = accounts.create(None)
    val t = transfers.create(src.id, dst.id, src.balance + src.limit.get)

    assertResult(Transfer.State.Pending) {
      t.state
    }

    assertResult(Some(t.copy(state = Transfer.State.Completed))) {
      transfers.get(t.id)
    }

    assertResult(dst.balance + t.amount) {
      accounts.get(dst.id).get.balance
    }

    assertResult(src.balance - t.amount) {
      accounts.get(src.id).get.balance
    }
  }

  it should "reject transfers if balance + limit < amount" in {
    val src = accounts.incrementBalance(
      accounts.create(Some(42)).id,
      42
    )

    val dst = accounts.create(None)
    val t = transfers.create(src.id, dst.id, src.balance + src.limit.get + 1)

    assertResult(Transfer.State.Pending) {
      t.state
    }

    assertResult(Some(t.copy(state = Transfer.State.Rejected))) {
      transfers.get(t.id)
    }

    assertResult(dst.balance) {
      accounts.get(dst.id).get.balance
    }

    assertResult(src.balance) {
      accounts.get(src.id).get.balance
    }
  }
}