package type

import event.UserEvent
import kotlinx.coroutines.Deferred

/**
 * Represents a type alias for a user action function.
 *
 * This alias defines a function type that, when invoked, returns a `Deferred` result of a `UserEvent`.
 * It is used to encapsulate asynchronous user actions in contexts such as Pokémon battles,
 * where the function will eventually provide a user input event after completing its execution.
 */
typealias User1stActionFunc = () -> Deferred<UserEvent>