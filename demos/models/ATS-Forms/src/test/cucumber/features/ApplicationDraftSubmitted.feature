Feature: A partial FormAnswered event is received by FormService

When FormService receives a partial FormAnsered event and validate it (except required fields), then generate new application, store it, and submmit.

  Scenario: A FormAnswered event is received.
    Given a partial FormAnswered event with the following form properties:
      | id                  | kjhKLtgjl76ifg                                                                                                                |
      | contestId           | FkUy6FIUDg89HGfgR                                                                                                             |
      | applicantId         | ilGKUGF79hg76G                                                                                                                |
      | applicationId       | SGAGASHASha                                                                                                                   |
      | origin              | http://opentalent.es                                                                                                          |
      | submissionDate      | 2017-02-01T00:00:00                                                                                                           |
      | firstSubmissionDate | 2017-02-01T00:00:00                                                                                                           |
      | language            | ES                                                                                                                            |
      | status              | DRAFT                                                                                                                         |
      | formId              | form-1                                                                                                                        |
      | answers             | [{"questionId": "1", "text": "answer"}]                                                                                       |
    And a Form with the following properties:
      | formId              | form-1                                                                                                                        |
      | language            | ES                                                                                                                        |
      | sections            | [{"sectionId": "1", "text": "Personal Data", "questions": [{ "id":"1", "required":true, "type":"text", "text": "question"}]}] |
    When FormService receives the previous event
    And Store new draft Application
    Then Validate it except required fiedls and store it
    And FormService publishes 1 ApplicationDraftStored event(s) with the previous FormAnswered info
