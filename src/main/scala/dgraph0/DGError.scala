package dgraph0

enum DgError:
  case DgErr(reason: String)
  case ClientCreationFailed
  case HostDown
