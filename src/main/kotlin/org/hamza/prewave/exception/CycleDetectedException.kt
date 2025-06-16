package org.hamza.prewave.exception

import java.lang.RuntimeException

class CycleDetectedException() : RuntimeException("Edge would create a cycle")