package com.github.alexuf.money_transfers.service

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

import com.github.alexuf.money_transfers.model.Account

/**
  * Created by alexuf on 11/03/2017.
  */
trait AccountsStorageComponent {

  trait AccountsStorage {
    def get(id: Long): Option[Account]

    def create(limit: Option[Long]): Account

    def incrementBalance(id: Long, balance: Long): Account
  }

  def accounts: AccountsStorage

  class InMemAccountsStorage extends AccountsStorage {

    private val idSeq = new AtomicLong()

    private val storage = new ConcurrentHashMap[Long, Account]()

    override def get(id: Long): Option[Account] = {
      Option(storage.get(id))
    }

    override def create(limit: Option[Long]): Account = {
      val id = idSeq.incrementAndGet()
      val account = Account(
        id = id,
        balance = 0,
        limit = limit
      )
      storage.put(id, account)
      account
    }

    override def incrementBalance(id: Long, delta: Long): Account = {
      var account: Account = null
      var update: Account = null
      do {
        account = storage.get(id)
        update = account.copy(balance = account.balance + delta)
      } while (!storage.replace(id, account, update))
      update
    }
  }

}