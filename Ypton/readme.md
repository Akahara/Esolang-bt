## Ypton

Ypton is a language that is designed not to be used for more than a minute at a time.

It works as python with a few *minor* differences:
- Indentation is reversed, and alternates between spaces and tabs.
- Keywords must be in MiXeD cAsE, and the case continues between keywords (eg 2 consecutive `for`s must be written as `FoR` and `fOr`)
- If statements are too conventionnal, use `ifn't...otherwise` instead
- Variables are declared normally but must be referenced using a different regex every time, the regex must match a single variable
- Variables names must start with `$$` because php isn't enough
- standard operations symbols are too vague, use `x` for multiplication, `±` for addition and `!±` for substraction instead
- Functions calls must be written `(arg1,arg2,arg3)function`, as it is much more explicit
- `print` becomes `System.Kernel.cout.ifopened.writeAndCloseImediatelyAfter()`, again, because of explicitness
- Also that method may only be used once, because we don't need more
- A single `#import <regex>` statement can be used, that imports all matching files
- Number literals in the code are to be written in base 7
- From python, `:` becomes `...`
- `;` to end a line, or `!;` after a `...`
- For debugging purposes, the compiler will add a `print("line xx")` at every line 
- Every string literal uses a caesar cipher which key is the length of the string, this is designed to improve security as no-one will be able to read your code and extract strings
- For low level utilities, a `asm {...}` blocks is available, similar to C
- And of course comments are prohibited, because who needs them

There are also guidelines on how to write *good* Ypton code:
- Thou shalt not write 6 or more lines function
- Thou shalt not use any kind of standard library
- Thou shalt make your code the hardest of all to read and understand, to improve on confidentiality

The current implementation of Ypton has one or two tweeks compared to its specifications
- There is no `asm{}` block or `#import` statement
- ``` may be used to call standard library functions (shame on you if you use 'em!)
- The error messages are fun(ky)
- Compiler note: the tokenizer (lexer) was taken from another of my projects and badly adapted, don't try to understand the code too much

### Examples

**Hello, World**

```python
("<Y``c rKcf`X")`System.Kernel.cout.ifopened.writeAndCloseImediatelyAfter`;
```

**Truth machine**

```python
    $$bInput = (":_afeod`^VeYZ_X")`input`;
		IfNt $$bInpu. == '/'...!;
  ("/")`System.Kernel.cout.ifopened.writeAndCloseImediatelyAfter`;
		OtHeRwIsE...!;
  wHiLe True...!;
("0")`System.Kernel.cout.ifopened.writeAndCloseImediatelyAfter`;
```
