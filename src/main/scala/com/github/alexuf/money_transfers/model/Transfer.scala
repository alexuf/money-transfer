package com.github.alexuf.money_transfers.model

/**
  * Created by alexuf on 11/03/2017.
  */
case class Transfer(id: Long, src: Account, dst: Account, amount: Long, state: Transfer.State)

object Transfer {

  sealed trait State {
    def name: String

    override final def toString: String = name
  }

  object State {

    case object Pending extends State {
      override val name: String = "pending"
    }

    case object Completed extends State {
      override val name: String = "completed"
    }

    case object Rejected extends State {
      override val name: String = "rejected"
    }

    val states = Set(Pending, Completed, Rejected)

    def fromString(value: String): State = states
      .find(_.name == value)
      .getOrElse(throw new IllegalArgumentException(s"Illegal state name $value"))
  }

}
