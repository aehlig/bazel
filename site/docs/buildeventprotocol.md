---
layout: documentation
title: Build Event Protocol
---

# Build Event Protocol

Bazel optionally provides a machine readable stream of important build events.
It is intended for external tools to monitor and report on a running build.

## Event Chaining

Each event has an event ID that is unique within a build invocation. Event IDs
are structured and for some types of events it is required that certain subparts
be present in the ID of that event (e.g., for an event indicating the completion
of a target, the label part need of the ID needs to be present as it indicates
which target was completed).

Each event also announces of which events it is a logical parent(!) of. The
stream of events guarantees a weak ordering principle: for each event that is
not the initial event of a build, at least one parent of the event was reported
before. In this way, the only events that come unexpectedly are those indicating
the beginning of a new build. The set of events for a build is complete if and
only if all announced children have been seen.

### Rationale for the Chosen Presentation of Dependency Edges

The evaluation in Skyframe is organised in a directed acyclic graph (DAG). While
not all nodes of the that DAG are represented in the event stream, the
dependency relation between those nodes that are presented is important. In
other words, the events naturally form a DAG.

Besides the chosen way of presenting this DAG structure, other natural ways are
having edges as first class citizens (which we didn't choose as having to
deal with two kind of entities seems to complicate the protocol even more), and
having each node declare who its parents are. Also, it might seem desirable that
all (and not just one) parent be present before an event apprears.

However, both, children declaring their parents and all parents appearing
before their children, do not work well with late discovery of dependencies.
We want to report events like completion of a test as soon as they
happened. At that moment, other targets might not be resolved yet; one of those
not-yet-resolved targets might well include a test suite containing the already
finished test. So that test will get a new parent. A similar situation arises
if a build failure, e.g., of a library, is reported and a target still
unresolved at that moment later turns out to depend on that library as well.

### Progress Events

As the event stream should also be usable by IDEs, summarized updates on the
progress of the build process (e.g., number of actions completed so far, number
of actions running at the moment) may also reported regularly. These events
follow the same chaining pattern as all other events: the first progress update
event is announced as a child of the initial event and each progress update
event that does not report that the build has been finished announces another
progress update event as its child.

This chain of progress events also solves the chaining problem of failed
actions. Logically, actions are children of the build events they belong to.
However, we do not want to report every action
happening, but all failing ones (and we do not know ahead of time which actions
will fail); so an event about the completion of the building of a target
can only be reported, once it is clear
which of the actions belonging to that target to build fail. Nevertheless, we
want to report the failure of an action as soon as it happens. Since
late-coming parents cannot be avoided anyway, we can as well have all
events reporting about the completion of a target come late as aprents and let
another progress event (properly chained in the stream of progress events)
announce the report about a failed acation as initial parent.

## Order Guarantees

Besides the weak order principle already mentioned (at least one parent of every
event comes before the actual event), the following additional properties on the
order of events are made, to make make consuming the stream more easy.

* If an event reports about the completion of a target mentions events about
  failed actions (the root causes) as logical children, those events have to
  come first in the stream.

* If an event reports about a summary of running a test, then all the events
  reporting about test actions that went into the summary have to come first
  in the stream.

* If an event reporting about the completion of a target refers to a specific
  configuration in its event id, an event describing that configuration has to
  come first in the stream.
