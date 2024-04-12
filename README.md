## DDT-Training - Kata

This is a Kata that aims to familiarize people with the concept of Domain-Driven Tests. If you don't know anything about DDTs, refer to [this talk](https://www.youtube.com/watch?v=Fk4rCn4YLLU) or [this document.](https://docs.google.com/document/d/10WuPeP-Ek2UKplnZg9zVp4UxGSejOfMnwPh-5bwxUVU/edit#heading=h.3gx79w623ped)

To summarize DDTs, it's focus is to test the domain logic, leaving implementation details to the last possible moment. They match perfectly with the steel thread/iterative approach, where we anticipate integration by using fakes/dummies, focusing only in the domain logic.

### Exercise

1) Add the concept of an Amendment Draft. After a Draft is submitted, an editor will request an amendment and the submitter will need to submit the Amendment Draft again.
2) Expand the existing Draft by creating a PrePopulatedDraft that will call a separate service to extract its metadata. Here's the contract on the extraction service:

```kotlin
fun extractMetadata(id: ManuscriptId): Result<Error, PrePopulatedDraft>
```
3) Expand the app to receive commands from a CLI interface.
3) Expand the app and expose it in an HTTP API.
