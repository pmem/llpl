# Contributing

## Issues

Here you'll find instructions on how to contribute to the Low-Level Persistence Library.

Your contributions are most welcome!  You'll find it is best to begin
with a conversation about your changes, rather than just writing a bunch
of code and contributing it out of the blue.
A great way to suggest new features, offer to add a feature,
or just begin a dialog about the Low-Level Persistence Library is to open an issue in our [GitHub Issues Database](https://github.com/pmem/llpl/issues)


### Contribution Guide

We accept contributions as pull requests on GitHub. Please follow these simple rules: 

* A PR should have a clear purpose, and do one thing only, and nothing more. This will enable us review your PR more quickly.
* Each commit in PR should be a small, atomic change representing one step in development.
* Please squash intermediate steps within PR for bugfixes, style cleanups, reversions, etc., so they would not appear in merged PR history.
* Please explain anything non-obvious from the code in comments, commit messages, or the PR description, as appropriate.

### License

The Low-Level Persistence Library is licensed under the terms in [LICENSE](https://github.com/pmem/llpl/blob/master/LICENSE). By contributing to the project, you agree to the license and copyright terms therein and release your contribution under these terms.

### Sign your work

Please use the sign-off line at the end of the patch. Your signature certifies that you wrote the patch or otherwise have the right to pass it on as an open-source patch. The rules are pretty simple: if you can certify
the below (from [developercertificate.org](http://developercertificate.org/)):

```
Developer Certificate of Origin
Version 1.1

Copyright (C) 2004, 2006 The Linux Foundation and its contributors.
660 York Street, Suite 102,
San Francisco, CA 94110 USA

Everyone is permitted to copy and distribute verbatim copies of this
license document, but changing it is not allowed.

Developer's Certificate of Origin 1.1

By making a contribution to this project, I certify that:

(a) The contribution was created in whole or in part by me and I
    have the right to submit it under the open source license
    indicated in the file; or

(b) The contribution is based upon previous work that, to the best
    of my knowledge, is covered under an appropriate open source
    license and I have the right under that license to submit that
    work with modifications, whether created in whole or in part
    by me, under the same open source license (unless I am
    permitted to submit under a different license), as indicated
    in the file; or

(c) The contribution was provided directly to me by some other
    person who certified (a), (b) or (c) and I have not modified
    it.

(d) I understand and agree that this project and the contribution
    are public and that a record of the contribution (including all
    personal information I submit with it, including my sign-off) is
    maintained indefinitely and may be redistributed consistent with
    this project or the open source license(s) involved.
```

Then you just add a line to every git commit message:

    Signed-off-by: Joe Smith <joe.smith@email.com>

Use your real name (sorry, no pseudonyms or anonymous contributions.)

If you set your `user.name` and `user.email` git configs, you can sign your
commit automatically with `git commit -s`.

### Bug Reports

Bugs for the Low-Level Persistence Library project are tracked in our
[GitHub Issues Database](https://github.com/pmem/llpl/issues).

When creating a bug report issue, please provide the following information:

#### Milestone field

Optionally, assign the milestone the issue needs to be fixed before.

#### Type: Bug label

Assign the `Type: Bug` label to the issue
(see [GitHub Help](https://help.github.com/articles/applying-labels-to-issues-and-pull-requests) for details).

#### Priority label

Optionally, assign one of the Priority labels (P1, P2, ...).
The Priority attribute describes the urgency to resolve a defect
and establishes the time frame for providing a verified resolution.
These Priority labels are defined as:

* **P1**: Showstopper bug, requiring resolution before the next release of the library.
* **P2**: High-priority bug, requiring resolution although it may be decided that the bug does not prevent the next release of the library.
* **P3**: Medium-priority bug.  The expectation is that the bug will be evaluated and a plan will be made for when the bug will be resolved.
* **P4**: Low-priority bug, the least urgent.  Fixed as resources are available.

Then describe the bug in the comment fields.

#### Type: Feature label

Assign the `Type: Feature` label to the issue, then describe the feature request in comment fields.

#### LLPL version

In a bug comment, put the LLPL version running when the bug was discovered.
