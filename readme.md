Esolangs
============

This repository is a collection of my completed [Esolangs](https://esolangs.org). Esolangs are basically esoteric programming languages, they are not meant to be used in production but they can be fun to play with and can prove useful to prove things regarding what can be done with a minimal set of instructions/operations.

## bt

The bt language (bit, but short) is a stack based language where the only values that exist are 0 and 1.

The main point of bt is that is has only one concrete data-manipulation instruction -- **nand** -- and yet it is turring complete.

Instructions are written in sequence, white space and new lines do not matter. The interpeter executes instructions by searching for the shortest instruction that matches the string at the current position.

For example:
```
// comments start at '//' or '#' and end at the end of the line

// function declarations, here they all have an empty body
function[]
anotherfunction[]
another[]

// these two lines are equivalent
function function
functionfunction
// anotherfunction cannot be called as the interpreter sees 'another'
// before it tries to read 'anotherfunction' so the next line will 
// run 'another' and then 'function'
anotherfunction
```

> Although *numbers* do not exist in bt, some instructions allow for IO of bit strings (ie the `/d8` instruction will print an integer which bits are the top 8 most bits of the stack), all these instructions consider the most significant bit to be the furthest on the stack.

### db instruction set:
#### Standard instructions
| instruction | meaning |
| ----------- | ------- |
| `!` | push 0 on the stack |
| `@` | pop n1 and n2 and push n1 *nand* n2 |
| `,` | noop, can be used to log the stack content in verbose mode |
| `;` | noop |
| `^`*n* | pop the *n*th bit from the stack (0 indexed) |
| *n* | copies and push the *n*th bit from the stack |

Note that the only bit manipulation instruction is *nand* !

The *n* parameter can be a single digit, or multiple if enclosed by single quotes:
```
0 // duplicate the top-most bit
22 // duplicate the 3rd and 2nd bits from the stack, equivalent to 2 2
'22' // duplicate the 23rd bit from the stack
^'10' // remove the 11th bit from the stack
```

#### Control flow instructions
| instruction | meaning |
| ----------- | ------- |
| `if` | pop a bit and if it is not 1 jump to the next `fi` instruction |
| `else` | pop a bit and if it is not 0 jump to the next `esle` instruction |
| `fi`, `esle` | noop |
| `back` | jumps to the begining of the function |
#### IO instructions
| instruction | meaning |
| ----------- | ------- |
| `.d`*n* | pop and print the top *n*th bits as a number |
| `.c`*n* | pop and print the top *n*th bits as an ascii character |
| `/b` | read and push '0' or '1' from standard input, same as `/d1` |
| `/d`*n* | read a number from standard input and push its binary representation in *n* bits |
| `/c`*n* | read a character from standard input and push its ascii representation in *n* bits |

### db functions

Functions are defined by their names and their bodies:
```
swap[02^2^2]
```
They can be called as any other instruction, they can be referenced whenever.

### Example programs

#### Standard operations up to 8 bit addition
```
one[!!@] // push 1 on the stack
zero[!]  // push 0 on the stack
not[0@]
and[@0@]
swap[02^2^2]
or[0@12@@^1]
xor[11@swap1@22@@^1^1]
xor[11@02@14@@^1^1^1]
cycle8[7^8]
cyclen8[7^8 7^8 7^8 7^8 7^8 7^8 7^8]
cycle3[2^3]
cyclen3[2^3 2^3]
reverse3[0 2 4 ^3^3^3]
reverse8[0 2 4 6 8 '10' '12' '14' ^8^8^8^8^8^8^8^8]
dup2[11]
dup3[222]
dup8[77777777]

// takes 2 bits from the stack and pushes their sum back on
// the stack. the first bit is the addition bit and the second
// (on top of the stack) is the carry bit.
addWcarry[11xor cycle3 cycle3 and]

// 2 bits addition, takes 2 2-bits numbers off the stack and
// pushes the result of their addition.
add2[
  3 2 4 3 ^4^4^4^4                               // reorder bits
  addWcarry                                      // add the first 2 lowest bits
  3^4 3^4 addWcarry cyclen3 addWcarry cycle3 or  // perform a full addition
  ^0 swap]                                       // remove the trailing carry bit and reverse

add8[
   '15'  '8 '  '16'  '9 '  '17'  '10'  '18'  '11'
   '19'  '12'  '20'  '13'  '21'  '14'  '22'  '15'
  ^'16' ^'16' ^'16' ^'16' ^'16' ^'16' ^'16' ^'16'
  ^'16' ^'16' ^'16' ^'16' ^'16' ^'16' ^'16' ^'16'
  addWcarry
  3^4    3^4    addWcarry cyclen3 addWcarry cycle3 or
  4^5    4^5    addWcarry cyclen3 addWcarry cycle3 or
  5^6    5^6    addWcarry cyclen3 addWcarry cycle3 or
  6^7    6^7    addWcarry cyclen3 addWcarry cycle3 or
  7^8    7^8    addWcarry cyclen3 addWcarry cycle3 or
  8^9    8^9    addWcarry cyclen3 addWcarry cycle3 or
  9^'10' 9^'10' addWcarry cyclen3 addWcarry cycle3 or
  ^0 reverse8]
  
one zero zero one  zero zero zero one  dup8.d8
zero one zero one  one  zero zero zero dup8.d8
add8.d8
```

#### Hello, World!
```
!!@!!!!@!!!.c7
!!@!!@!!!!@!!!@.c7
!!@!!@!!!@!!@!!.c7
!!@!!@!!!@!!@!!.c7
!!@!!@!!!@!!@!!@!!@.c7
!!!@!!!@!!@!!.c7
!!!@!!!!!.c7
!!@!!!@!!!@!!@!!@.c7
!!@!!@!!!@!!@!!@!!@.c7
!!@!!@!!@!!!!@!.c7
!!@!!@!!!@!!@!!.c7
!!@!!@!!!!@!!.c7
!!!@!!!!!!@.c7
!!@!!!@!.c4
```
#### [Thruth machine](https://esolangs.org/wiki/Truth-machine)
```
print1infenitely[!!@.d1back]
/b 0if
  print1infenitely
fi else
  !.d1
esle
```

