import models.HelpSTAR
import models.HelpSTAR._
import org.specs2.mutable._
import xml.Node


class HelpSTARUnit extends Specification {

  "Detail Parsing" should {
    val properties = HelpSTAR.parseDetails(get_ticket_HTML(5432, "details.html"))
    "parse example details to not be empty" in {
      properties.size must_!= 0
    }
    "parse example title is Cannot Register with Given Perm & PW" in {
      properties("Title") mustEqual ("Cannot Register with Given Perm & PW")
    }

    "parse example number to 5432" in {
      properties("Number") mustEqual ("5432")
    }

    "parse example to a status that has Closed" in {
      properties("Status") must contain("Closed")
    }

    //    "parse transactions" in {
    //      "Hello world" must startWith("Hello")
    //    }
    //    "parse UDFs" in {
    //      "Hello world" must endWith("world")
    //    }
  }



  "Transaction Parsing" should {
    val transactions = HelpSTAR.parseTransactions(get_ticket_HTML(5432, "transactions.html")
    )
    "parse last transaction to not be empty" in {
      transactions.size must_!= 0
    }

    "parse last transaction's first username is Danny'" in {
      transactions(0).who mustEqual "Danny Phillips"
    }

    "parse last transaction to have one memo" in {
      transactions(0).memos.size mustEqual 1
    }

    "parse last transaction's memo to be a workflow" in {
      transactions(0).memos(0).kind must contain("Workflow")
    }

    "parse last transaction's memo to contain closed in the content" in {
      transactions(0).memos(0).content must contain("CLOSED")
    }

    "parse business rules transaction to Business Rule Service" in {
      transactions(3).who mustEqual "Business Rule Service"
    }

    "parse first example transaction to correct time" in {
      transactions(0).time mustEqual "7/3/2012 2:11:07 PM"
    }

    "parse business rules transaction to correct time" in {
      transactions(3).time mustEqual "6/26/2012 8:24:29 AM"
    }

    "parse business rules transaction to have two memos" in {
      transactions(3).memos.size mustEqual 2
    }

    "parse first transaction to have two memos" in {
      transactions.last.memos.size mustEqual 2
    }

    "parse first transaction to have a memo with Carreros in it" in {
      transactions.last.memos.last.content must contain("Carreros")
    }


  }


  "User Defined Fields Parsing" should {
    val user_defined_fields = HelpSTAR.parseUDF(get_ticket_HTML(5432, "udf.html"))
    "parse number of username defined fields to 0" in {
      user_defined_fields.size mustNotEqual 0
    }
    "parse PERM Number to 0000000" in {
      user_defined_fields.getOrElse("PERM Number", "N/A") mustEqual "0000000"
    }

    "parse Residence Hall or Apartment Building to San Clemente" in {
      user_defined_fields.
        getOrElse("Residence Hall or Apartment Building", "N/A") mustEqual "San Clemente"
    }
  }
}