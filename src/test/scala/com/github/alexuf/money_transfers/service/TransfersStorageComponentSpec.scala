package com.github.alexuf.money_transfers.service

import com.github.alexuf.money_transfers.TestEnv
import com.github.alexuf.money_transfers.model.Transfer
import org.mockito.Mockito._
import org.scalatest.FlatSpec

/**
  * Created by alexuf on 13/03/2017.
  */
class TransfersStorageComponentSpec extends FlatSpec with TestEnv {

  it should "return None if absent" in {
    assertResult(None) {
      transfers.get(-1)
    }
  }

  it should "return transfers if present" in {
    val src = accounts.create(None)
    val dst = accounts.create(None)
    val t = transfers.create(src.id, dst.id, 42)

    assertResult(Some(t)) {
      transfers.get(t.id)
    }
  }


  it should "schedule transfer processing after create" in {
    val src = accounts.create(None)
    val dst = accounts.create(None)
    val t = transfers.create(src.id, dst.id, 42)

    verify(transferProcessor, times(1)).schedule(t)
  }


  it should "update transfer state if pending" in {

    val src = accounts.create(None)
    val dst = accounts.create(None)
    val t = transfers.create(src.id, dst.id, 42)

    val update = transfers.updateState(t.id, Transfer.State.Completed)

    assertResult(Transfer.State.Completed) {
      update.state
    }

    assertResult(Some(update)) {
      transfers.get(t.id)
    }
  }

  it should "intercept final state update" in {

    val src = accounts.create(None)
    val dst = accounts.create(None)
    val t = transfers.create(src.id, dst.id, 42)

    transfers.updateState(t.id, Transfer.State.Completed)

    intercept[IllegalStateException] {
      transfers.updateState(t.id, Transfer.State.Rejected)
    }
  }

  it should "automatically generate ids" in {

    val src = accounts.create(None)
    val dst = accounts.create(None)
    val t1 = transfers.create(src.id, dst.id, 42)
    assert(t1.id != 0)

    val t2 = transfers.create(src.id, dst.id, 42)
    assert(t1.id != t2.id)
  }
}