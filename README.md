## DDT-Training - Kata

This is a Kata that aims to familiarize people with the concept of Domain-Driven Tests. If you don't know anything about DDTs, refer to [this talk](https://www.youtube.com/watch?v=Fk4rCn4YLLU) or [this document.](https://docs.google.com/document/d/10WuPeP-Ek2UKplnZg9zVp4UxGSejOfMnwPh-5bwxUVU/edit#heading=h.3gx79w623ped)

To summarize DDTs, its focus is to test the domain logic, leaving implementation details to the last possible moment. They match perfectly with the steel thread/iterative approach, where we anticipate integration by using fakes/dummies, focusing only in the domain logic.

### What is this app about?

To keep it really simple, this is the good&old store app. At the moment, these are the scenarios implemented:

- A manager can register new products into the catalog.
- A customer can see the product catalog.
- A customer can buy a product if it has stock.
 
### Exercises

1) To improve your store's security, make sure the manager is _really_ a manager by asking for a password when they log in. If the password matches `admin123`, then we can trust they're a manager!
2) Some products sold in your store can only be bought by adults (cigarettes, beer, wine). Make sure your store complies with the law!