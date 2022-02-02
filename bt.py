import re
import sys

class Stack:
  def __init__(self):
    self.stack = []

  def ensure_valid_index(self, n):
    if len(self.stack) <= n:
      raise RuntimeError("Stack size < " + str(n))

  def pop(self):
    if len(self.stack) == 0:
      raise RuntimeError("Stack is Empty")
    else:
      return self.stack.pop()

  def popn(self, n):
    self.ensure_valid_index(n-1)
    return [self.pop() for _ in range(n)]

  def dup(self, n):
    self.ensure_valid_index(n)
    self.stack.append(self.stack[-n-1])
    
  def drop(self, n):
    self.ensure_valid_index(n)
    del self.stack[-n-1]

  def push(self, *bits):
    for bit in bits:
      if bit != 0 and bit != 1:
        raise RuntimeError("Cannot push a non-bit on the stack")
      self.stack.append([0, 1][bit])

  def __str__(self):
    return str(self.stack)


class CodeSequence:
  def __init__(self, source):
    self.ist = []
    self.symbols = {}
    source = re.sub('[\n ]|(//|#).*', '', source)
    functions = re.findall(r'(([a-zA-Z0-9]+)\[([^]]*)\])', source)
    for group in functions:
      (whole, name, body) = group
      self.symbols[name] = body
      source = source.replace(whole, '')
    self.ist.append([source, 0])

  def is_in_subsymbol(self):
    return len(self.ist) > 1

  def next(self):
    [line, index] = self.ist[-1]
    while index == len(line):
      self.ist.pop()
      if len(self.ist) == 0:
        return None
      [line, index] = self.ist[-1]
    self.ist[-1][1] += 1
    return line[index]

  def next_digit(self):
    c = self.next()
    if c is None:
      raise RuntimeError("Expected trailing digit")
    if c == '\'':
      d = 0
      c = self.next()
      while c != '\'':
        d = 10*d+as_digit(c)
        c = self.next()
      return d
    return as_digit(c)
  
  def put_back_once(self):
    self.ist[-1][1] -= 1

  def back(self):
    self.ist[-1][1] = 0

  def skip_to(self, tag):
    [line, index] = self.ist[-1]
    i = line.find(tag, index)
    if i == -1:
      raise RuntimeError("Could not find '" + tag + "'")
    self.ist[-1][1] = i+len(tag)
    # print("Remaining:", self.ist[-1][0][self.ist[-1][1]:])

  def get_symbol(self, name):
    return self.symbols[name] if name in self.symbols else None
  
  def run_symbol(self, symbol):
    self.ist.append([symbol, 0])


def is_digit(c):
  return len(c) == 1 and '0' <= c <= '9'
    
def as_digit(c):
  if not is_digit(c):
    raise RuntimeError("Not a digit: " + c)
  return ord(c) - ord('0')

def bits2num(bits):
  return sum(2**i for i,b in enumerate(bits) if b)

def num2bits(num, bitcount):
  if not 0 <= num < 2**bitcount:
    raise RuntimeError("Out of range")
  return reversed([(1 if num & 2**i else 0) for i in range(bitcount)])


def run(code, verbose=1):
  stack = Stack()
  code = CodeSequence(code)
  if verbose > 0:
    for s in code.symbols:
      print('Symbol', s, '--', code.symbols[s])
  symbol = ''
  c = code.next()
  while c is not None:
    symbol += c
    sym = code.get_symbol(symbol)
    executed = True
    if symbol == '!':
      stack.push(0)
    elif symbol == '@':
      x1, x2 = stack.pop(), stack.pop()
      stack.push(not (x1 and x2))
    elif symbol == '.d':
      n = code.next_digit()
      symbol += str(n)
      xs = stack.popn(n)
      print(bits2num(xs))
    elif symbol == '.c':
      n = code.next_digit()
      symbol += str(n)
      xs = stack.popn(n)
      print(chr(bits2num(xs)), end='')
    elif symbol == '/b':
      print('/b=',end='')
      x = sys.stdin.read(1)
      if x not in '01':
        raise RuntimeError("Expected 0 or 1")
      stack.push(as_digit(x))
    elif symbol == '/d':
      n = code.next_digit()
      symbol += str(n)
      print(symbol+'=',end='')
      try:
        x = int(input())
        stack.push(*num2bits(x, n))
      except ValueError:
        raise RuntimeError("Expected an 8 bits number")
    elif symbol == '/c':
      n = code.next_digit()
      symbol += str(n)
      print(symbol+'=',end='')
      x = ord(sys.stdin.read(1))
      stack.push(*num2bits(x, n))
    elif symbol in ',;':
      pass
    elif symbol == '^':
      n = code.next_digit()
      symbol += str(n)
      stack.drop(n)
    elif is_digit(symbol) or symbol == '\'':
      code.put_back_once()
      n = code.next_digit()
      stack.dup(n)
    elif symbol == 'if':
      n = stack.pop()
      if not n:
        code.skip_to('fi')
    elif symbol == 'fi':
      pass
    elif symbol == 'else':
      n = stack.pop()
      if n:
        code.skip_to('esle')
    elif symbol == 'esle':
      pass
    elif symbol == 'back':
      code.back()
    elif sym is not None:
      if verbose > 0:
        print('Call', symbol)
      code.run_symbol(sym)
    else:
      executed = False

    if executed:
      if (verbose > 1 if code.is_in_subsymbol() else verbose > 0) or symbol == ',':
        print(symbol, stack)
      symbol = ''
    c = code.next()
  if len(symbol):
    print('Ignored', symbol)
  print('Ended with', stack)
  

run("""\
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
// pushes the result of their addition. Most significant bits
// must be stored further down the stack
add2[
  3 2 4 3 ^4^4^4^4                               // reorder bits
  addWcarry                                      // add the first 2 lowest bits
  3^4 3^4 addWcarry cyclen3 addWcarry cycle3 or  // perform a full addition
  ^0 swap]                                       // remove the trailing carry bit and reverse

add3[
  5 3 6 4 7 5 ^6^6^6^6^6^6
  addWcarry
  3^4 3^4 addWcarry cyclen3 addWcarry cycle3 or
  4^5 4^5 addWcarry cyclen3 addWcarry cycle3 or
  ^0 reverse3]

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

println[one zero one zero .c4]

# one one dup2.d2
# one one dup2.d2
# add2.d2
# one zero zero one  zero zero zero one  dup8.d8
# zero one zero one  one  zero zero zero dup8.d8
# add8.d8
# one  zero one  dup3.d3
# zero zero one  dup3.d3
# add3.d3

print1infenitely[one.d1back]
truthmachine[
  /b 0if
    print1infenitely
  fi else
    !.d1
  esle
]

# truthmachine;

helloworld[
  one  zero zero one  zero zero zero .c7
  one  one  zero zero one  zero one  .c7
  one  one  zero one  one  zero zero .c7
  one  one  zero one  one  zero zero .c7
  one  one  zero one  one  one  one  .c7
  zero one  zero one  one  zero zero .c7
  zero one  zero zero zero zero zero .c7
  one  zero one  zero one  one  one  .c7
  one  one  zero one  one  one  one  .c7
  one  one  one  zero zero one  zero .c7
  one  one  zero one  one  zero zero .c7
  one  one  zero zero one  zero zero .c7
  zero one  zero zero zero zero one  .c7
  println
]

# helloworld;

""", verbose=0)