INPUT "Input:",V4
t0 = 20
V5 = t0
t2 = V5
t3 = 4
t1 = t2 - t3
V6 = t1
PRINT V6
t4 = V4
t5 = V6
IF t4 = t5 THEN GOSUB L1 ELSE GOSUB L2

L1:
END
PRINT "Hellyeah"
GOTO L3

L2:
PRINT "No"
STOP

L3:
t6 = V4
V6 = t6
t7 = F1(V4, V5, V6)
V7 = t7
PRINT V7
STOP
END

FUNCTION F1 (t8, t9, t10)
V4 = t8
V5 = t9
V6 = t10
t12 = V4
t13 = V5
t11 = t12 + t13
V7 = t11
F1 = V7
END FUNCTION
FUNCTION F2 (t14, t15, t16)
V4 = t14
V5 = t15
V6 = t16
t17 = V4
V7 = t17
t18 = V5
V8 = t18
t20 = V7
t21 = V8
t19 = t20 * t21
V9 = t19
F2 = V9
END FUNCTION