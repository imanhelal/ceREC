---
title: "R Notebook for Paper's Statistical Tests"
output: Significance Tests for Logs: Synthetic, BPI2013-Closed, WABO CoSeLoG
---

This is an [R Markdown](http://rmarkdown.rstudio.com) Notebook. When you execute code within the notebook, the results appear beneath the code. 

Try executing this chunk by clicking the *Run* button within the chunk or by placing your cursor inside it and pressing *Ctrl+Shift+Enter*. 

Add a new chunk by clicking the *Insert Chunk* button on the toolbar or by pressing *Ctrl+Alt+I*.

When you save the notebook, an HTML file containing the code and output will be saved alongside it (click the *Preview* button or press *Ctrl+Shift+K* to preview the HTML file).

The preview shows you a rendered HTML copy of the contents of the editor. Consequently, unlike *Knit*, *Preview* does not run any R code chunks. Instead, the output of the chunk when it was last run in the editor is displayed.

# Synthetic log Welch t test

Need to load Excel files from folder Loop1000

```{r}
t.test(x=ceREC_loop1000 , y=REC_loop1000, var.equal=FALSE, paired = FALSE, mu = 0)

```


# Synthetic log Wilcoxon test
```{r}
x <- read.csv(file="../Loop1000/ceREC_loop1000.csv", header=TRUE, sep = "," , "rt")
y <- read.csv(file="../Loop1000/REC_loop1000.csv", header=TRUE, sep = "," , "rt")
ceREC <- print(x)
REC <- print(y)
y <- as.numeric(ceREC$Diff)
x <- as.numeric(REC$diff_ms)
wilcox.test( x, y , alternative = "two.sided", paired = FALSE, mu=0)


```



# BPI2013-Closed log Welch t test

Need to load Excel files from folder BPI2013-Closed

```{r}
t.test(x=ceREC_BPI2013Closed , y=REC_BPI2013Closed, var.equal=FALSE, paired = FALSE, mu = 0)

```

# BPI2013-Closed log Wilcoxon test
```{r}
x <- read.csv(file="../BPI2013Closed/ceREC_BPI2013Closed.csv", header=TRUE, sep = "," , "rt")
y <- read.csv(file="../BPI2013Closed/REC_BPI2013Closed.csv", header=TRUE, sep = "," , "rt")
ceREC <- print(x)
REC <- print(y)
y <- as.numeric(ceREC$Diff)
x <- as.numeric(REC$diff_ms)
wilcox.test( x, y , alternative = "two.sided", paired = FALSE, mu=0)


```

# WABO CoSeLoG log Welch t test

Need to load Excel files from folder CoSeLoG

```{r}
t.test(x=ceREC_CoSeLoG , y=REC_CoSeLoG, var.equal=FALSE, paired = FALSE, mu = 0)

```

# WABO CoSeLoG log Wilcoxon test
```{r}
x <- read.csv(file="../CoSeLoG/ceREC_CoSeLoG.csv", header=TRUE, sep = "," , "rt")
y <- read.csv(file="../CoSeLoG/REC_CoSeLoG.csv", header=TRUE, sep = "," , "rt")
ceREC <- print(x)
REC <- print(y)
y <- as.numeric(ceREC$Diff)
x <- as.numeric(REC$diff_ms)
wilcox.test( x, y , alternative = "two.sided", paired = FALSE, mu=0)


```