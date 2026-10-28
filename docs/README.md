# Submission document drafts

App: Simple Dictionary. Course: BSIT-SD, 2nd year. Section: IDB1-SD.

- Requirements.docx / Requirements.pdf: BA requirements, user stories, acceptance criteria and text wireframes.
- QA.docx / QA.pdf: test plan, planned manual cases, recorded automated results, bug log and sign-off fields.
- Requirements.md / QA.md: plain-text editable source drafts.
- evidence/: copies of the existing six passing automated-test results. These are earlier run records, not final-release certification.

## Before submission

1. Confirm with the instructor whether the reported four-member team is acceptable; the exam specifies three members.
2. Fill in names privately, the group/instructor details, dates and actual contributions. Do not invent past contributions or create misleading commit history.
3. Review the requirements against the final app and replace text wireframes with screenshots if desired or required by the instructor.
4. Test on an Android emulator/device and record its model/AVD, Android version/API and final revision.
5. Execute the manual cases. Fill actual outcomes, Pass/Fail/Blocked, tester/date and evidence references. They are currently Not run.
6. Rerun both automated suites against the frozen code and replace/add final evidence. The existing XML files are explicitly labelled recorded earlier results.
7. Retest BUG-001 manually and record the actual outcome; do not close it solely on an assumed successful desktop run.
8. Build/verify the APK or emulator instructions. Add the final commit, review/sign-off and repository link to the submission.
9. Review the PDFs for pagination and missing information after all edits.

## Editing

Open the .docx files in Word or LibreOffice and edit the blanks, then export updated PDFs. The .md files are another editable format; they are not automatically synchronized with later Word edits. Keep the chosen final source and PDF consistent. The supplied PDFs remain drafts while the completion fields and tests are outstanding.

No names or completed manual test results were supplied when these drafts were created. No new automated tests were executed to generate these documents. The QA package uses the existing local JUnit reports, and records general desktop testing as user-reported only.

## Beginner learning documents

- BeginnerGuide.pdf: start here for the plain-language overview, Kotlin vocabulary and search/favorite/preference data flows.
- BeginnerGuide.docx / BeginnerGuide.md: editable versions of the overview.
- CodeWalkthrough.html: open in a browser for a searchable, offline, side-by-side code and explanation for every line in the selected handwritten source/configuration/test files. Expand files and use Show setup/brackets to reveal imports and closing syntax too.
- evidence/code-walkthrough-snapshot.json records the annotated files, their line counts and hashes. The guide snapshot is dated 28 October 2026; changes to source can make line references stale.

These are study documents, not additional proof of manual testing or a substitute for understanding the code. Binary launcher images, generated Room/build files, Gradle wrapper internals and Xcode project internals are excluded from the line-by-line source reference.

## Phone-friendly full walkthrough

The complete line-by-line reference is also available as [PDF and Word editions](phone-walkthrough/README.md), both as one full document and three smaller parts. These use a single-column, code-then-explanation layout and include all 2,237 annotated lines.
