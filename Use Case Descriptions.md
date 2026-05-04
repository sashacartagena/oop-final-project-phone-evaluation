## UC-01: Evaluate Phone

| | |
|---|---|
| **Overview** | The user requests to get a valuation for their phone. The system prompts them to enter information regarding the phone, and then the AI system uses that info to make a valuation. |
| **Related Use Cases** | Includes entering phone model, entering phone storage, and entering phone damage |
| **Actors** | Phone Evaluation User, Phone Evaluation System, AI Service |

## UC-02: Search for Phone

| | |
|---|---|
| **Overview** | The user selects to search for a phone and chooses to search either by model name or by price range.  |
| **Related Use Cases** | Generalization: Search by model, Search by price |
| **Actors** | Phone Evaluation User, Phone Evaluation System |

## UC-02a: Search by Model

| | |
|---|---|
| **Overview** | Specialized search where the user inputs a phone model name and the system displays matching results from the phone list. |
| **Related Use Cases** | Parent: UC-02 Search for Phone |
| **Actors** | Phone Evaluation User, Phone Evaluation System |

## UC-02b: Search by Price

| | |
|---|---|
| **Overview** | Specialized search where the user enters a minimum and maximum price range and the system displays matching results from the phone list. |
| **Related Use Cases** | Parent: UC-02 Search for Phone |
| **Actors** | Phone Evaluation User, Phone Evaluation System |

## UC-03: Browse All Phones

| | |
|---|---|
| **Overview** | The user selects to browse and the system retrieves and displays a list of all phones stored in the phone list. |
| **Related Use Cases** | None |
| **Actors** | Phone Evaluation User, Phone Evaluation System |