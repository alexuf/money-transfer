package com.github.alexuf.money_transfers.service

import com.github.alexuf.money_transfers.TestEnv
import org.scalatest.FlatSpec

/**
  * Created by alexuf on 13/03/2017.
  */
class AccountsStorageComponentSpec extends FlatSpec with TestEnv {

  it should "return None if absent" in {
    assertResult(None) {
      accounts.get(-1)
    }
  }

  it should "return Some if present" in {
    val a = accounts.create(None)
    assertResult(Some(a)) {
      accounts.get(a.id)
    }
  }

  it should "update accounts" in {

    val a = accounts.create(None)
    val update = accounts.incrementBalance(a.id, 42)

    assertResult(42) {
      update.balance
    }

    assertResult(Some(update)) {
      accounts.get(a.id)
    }

  }

  it should "automatically generate ids" in {

    val a1 = accounts.create(None)
    assert(a1.id != 0)

    val a2 = accounts.create(None)
    assert(a1.id != a2.id)
  }
}
