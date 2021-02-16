package clientApps.messageRegistry

case class Message(uid: String, message: String)
case class QueryMessageResults(queryMessagesResults: List[Message])