import XCTest
import Shared
@testable import RaceHub

/// The Swift side of error localization: every Kotlin error reason must map to
/// text, and a server refusal must show the server's own words.
final class ErrorMessagesTests: XCTestCase {

    func testDataErrorsHaveDistinctMessages() {
        let messages = [DataError.network, DataError.server, DataError.unknown].map(\.userMessage)
        XCTAssertEqual(messages[0], "Can't reach the server. Check your connection and try again.")
        XCTAssertEqual(messages[1], "The server had a problem. Please try again.")
        XCTAssertEqual(messages[2], "Something went wrong. Please try again.")
    }

    func testRejectedShowsTheServerMessage() {
        let failure = AuthFailure(reason: .rejected, serverMessage: "Invalid credentials")
        XCTAssertEqual(failure.userMessage, "Invalid credentials")
    }

    func testRejectedWithoutServerMessageFallsBackToGenericText() {
        let failure = AuthFailure(reason: .rejected, serverMessage: nil)
        XCTAssertEqual(failure.userMessage, "The request was declined. Please check your details.")
    }

    func testTransportFailuresReuseTheDataErrorText() {
        XCTAssertEqual(AuthFailure(reason: .network, serverMessage: nil).userMessage, DataError.network.userMessage)
        XCTAssertEqual(AuthFailure(reason: .server, serverMessage: nil).userMessage, DataError.server.userMessage)
        XCTAssertEqual(AuthFailure(reason: .unknown, serverMessage: nil).userMessage, DataError.unknown.userMessage)
    }

    /// The Swift switch matches Kotlin enum names, so a renamed or added entry
    /// would silently fall through to the generic text. Catch that here.
    func testEveryValidationReasonHasItsOwnMessage() {
        let generic = DataError.unknown.userMessage
        let validation = AuthError.entries.filter { ![AuthError.rejected, .network, .server, .unknown].contains($0) }
        XCTAssertFalse(validation.isEmpty)
        for reason in validation {
            let message = AuthFailure(reason: reason, serverMessage: nil).userMessage
            XCTAssertNotEqual(message, generic, "\(reason.name) has no message")
        }
        XCTAssertEqual(Set(validation.map { AuthFailure(reason: $0, serverMessage: nil).userMessage }).count, validation.count)
    }
}
