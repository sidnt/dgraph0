package clientApps.messageRegistry

case class Message(message: String)
case class QueryMessageResults(queryMessagesResults: List[Message])