f2c:
    LDURS S16, [X27,const5]   // S16 = 5.0 (5.0 in memory)
    LDURS S18, [X27,const9]   // S18 = 9.0 (9.0 in memory)
    FDIVS S16, S16, S18       // S16 = 5.0 / 9.0
    LDURS S18, [X27,const32]  // S18 = 32.0
    FSUBS S18, S12, S18       // S18 = fahr – 32.0
    FMULS S0, S16, S18        // S0 = (5/9)*(fahr – 32.0)
    BR LR                     // return
