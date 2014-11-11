
Performance comparisons (simple single project)

 - Maven v3.2.3 - 6.106 + 6.684 + 5.363 = Avg 6.051
 - Gradle v2.1 - 6.981 + 6.572 + 6.416 = Avg 6.656
 - SBT 0.13.5 - 3.706 + 3.770 + 3.710 = Avg 3.706
 - Blaze (no async) - 3.101 + 3.093 + 3.055 = Avg 3.083
 - Blaze (with async) - 2.746 + 2.652 + 2.725 = Avg 2.707

1.4 times faster than SBT
2.2 times faster than Maven
2.5 times faster than Gradle

