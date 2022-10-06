#!/usr/bin/env sh

# This file is the entry point to your application.
# We make use of the ApolloShimOpConfigHelpers run command - which will run the ApolloCmd scripts,
# and start your generated "run-service.sh" (Generated by Cloud9ApolloJavaWrapperGenerator, via your build.xml)
# It will pass "domain", "realm", and "root" to your application.
# When using containers, note that we don't have process manager. Instead, the container orchestrator will be the thing that monitors
# when a container instance dies - and will subsequently re-schedule a new container instance.
# In other words, this script is the long running entry-point, and when it exits, your container exits too.

exec /opt/amazon/bin/bones_run_apollo_shim.sh --script bin/run-service.sh