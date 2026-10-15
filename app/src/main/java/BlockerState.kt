object BlockerState {
    @Volatile var active: Boolean = false
    @Volatile var blockedPackages: Set<String> = emptySet()   // apps ticked for this session
}