# Marathon Testing

The Marathon project has several styles of tests that are maintained with the project that include:

* Unit Testing + Mocking
* CI Integration Testing
* Benchmark Testing
* System Testing

##  Unit Testing

The unit and mocking tests are part of the project.  The test code is managed in the
code base under `src/test` and are involved with `sbt test`.  These tests are whitebox tests and
are focused on testing the internals of Marathon.

These tests are run for each pull request as part of CI builds and are maintained with
[jenkins](https://velocity.mesosphere.com/service/velocity/view/Marathon/).

For version code of tests, the CI build is initiated with [jenkins/unit-integration-tests.sh](../jenkins/unit-integration-tests.sh)

## CI Integration Testing

In order to invoke the CI Integration tests.  This test are longer running more exhaustive testing
of marathon code with system mocks.

```
sbt integration/test
```

## Benchmark Testing

In order to run the benchmark tests:

```
sbt bench:test
```

This tests can take significantly time to run and provide performance benchmarks on things like
how long activities like saving to zk will take at different levels of scale.

## System Testing

The system integration tests are integration tests with [DCOS](http://dcos.io)
and verify system integration behavior from a user perspective using DCOS as the
context. The tests are written in Python using [shakedown](https://github.com/dcos/shakedown)
as the testing tool.

The tests require an installed Python 3 and pip3. You can then prepare an
virtualenv with

```
cd test
make init
```

The target uses [Pipenv](https://docs.pipenv.org/) under the hood.

The tests are run with

```
make test
```

You can also run a specific test module or function with

```
pipenv shell
dcos cluster setup <dcos ip>
shakedown test_marathon_root.py::<test name>
```

For more details checkout the [shakedown site](https://github.com/dcos/shakedown).
The tests are written under the project [test/system](system/README.md).

### Shakedown Patching

Shakedown is imported as a `[git-subrepo](https://github.com/ingydotnet/git-subrepo#readme)`
and declared as an editable dependency in the Pipfile. This allows any Marathon
contributor to also make edits to Shakedown!

If you made an edit simply commit it to the Marathon repository.

```
git subrepo push tests/shakedown -b foobar
```

will push all shakedown related commits to the `foobar` branch of the Shakedown
[repository](https://github.com/dcos/shakedown).

```
git subrepo pull tests/shakedown -b master
```

will pull all changes on the Shakedown master as a suqashed commit into the
Marathon repository. This will keep the Marathon commit history clean.

