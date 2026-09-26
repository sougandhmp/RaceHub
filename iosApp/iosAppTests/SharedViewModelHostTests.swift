import Combine
import KMPNativeCoroutinesCore
import XCTest
import Shared
@testable import RaceHub

/// `SharedViewModelHost` bridges Kotlin flows into SwiftUI and owns the
/// ViewModel's lifetime; these pin down both halves.
@MainActor
final class SharedViewModelHostTests: XCTestCase {

    private var subscriptions = Set<AnyCancellable>()

    override func tearDown() {
        subscriptions.removeAll()
        super.tearDown()
    }

    func testStateAndEffectsAreRelayedInOrder() async {
        let host = SharedViewModelHost<NSObject, Int, String>(
            create: { _ in NSObject() },
            value: { _ in 0 },
            flow: { _ in fakeFlow([1, 2, 3]) },
            effects: { _ in fakeFlow(["first", "second"]) }
        )
        var effects: [String] = []
        let effectsDone = expectation(description: "effects")
        host.effects.sink { effect in
            effects.append(effect)
            if effects.count == 2 { effectsDone.fulfill() }
        }.store(in: &subscriptions)
        let stateDone = expectation(description: "state")
        host.$state.first { $0 == 3 }.sink { _ in stateDone.fulfill() }.store(in: &subscriptions)

        await fulfillment(of: [effectsDone, stateDone], timeout: 2)
        XCTAssertEqual(effects, ["first", "second"])
    }

    func testIntentsReachTheSharedViewModel() async {
        let host = HomeModel.home()
        XCTAssertEqual(host.state.selectedTab, .race)
        let selected = expectation(description: "tab selected")
        host.$state.first { $0.selectedTab == .forum }.sink { _ in selected.fulfill() }.store(in: &subscriptions)

        host.send(HomeIntent.TabSelected(tab: .forum))

        await fulfillment(of: [selected], timeout: 2)
    }

    func testReleasingTheHostClearsItsViewModel() {
        var owner: ViewModelOwner?
        var host: HomeModel? = HomeModel(
            create: { owner = $0; return SharedViewModels.shared.home(owner: $0) },
            value: { $0.state },
            flow: { $0.stateFlow }
        )
        let viewModel = host!.viewModel
        XCTAssertTrue(SharedViewModels.shared.home(owner: owner!) === viewModel, "owner should keep its ViewModel while the host lives")

        weak var released = host
        host = nil

        XCTAssertNil(released, "the host must not be retained by its own collection tasks")
        XCTAssertFalse(SharedViewModels.shared.home(owner: owner!) === viewModel, "the owner's store should have been cleared")
    }
}

/// A finite native flow that emits `items` with back-pressure, like a Kotlin flow does.
/// Like a real flow, it resumes asynchronously: the collector calls `next` while
/// holding its lock, so emitting synchronously from there would deadlock.
private func fakeFlow<T>(_ items: [T]) -> NativeFlow<T, Error, KotlinUnit> {
    { onItem, onComplete, _ in
        var remaining = items[...]
        func emitNext() {
            guard let item = remaining.popFirst() else {
                _ = onComplete(nil, KotlinUnit.shared)
                return
            }
            _ = onItem(item, { DispatchQueue.global().async(execute: emitNext); return KotlinUnit.shared }, KotlinUnit.shared)
        }
        emitNext()
        return { KotlinUnit.shared }
    }
}
