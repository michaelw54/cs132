import sys

start = 'A'

grammar = [x.strip().replace(':==', '=').replace(' ', '').replace('ε', '') for x in sys.stdin.readlines()]
rules = {rule.split('=')[0] : rule.split('=')[1].split('|') for rule in grammar}

# Compute first set
first = {nt:set() for nt in rules.keys()}
for nt, prod in rules.items():
    for e in prod:
        if len(e) == 0: # epsilon
            first[nt].add('')
        elif e[0].islower(): # term
            first[nt].add(e[0])
        else: #nonterm
            for c in e: # continue adding firsts as long as we have nullable nonterms
                if c.islower():
                    first[nt].add(c)
                    break
                first[nt].add('('+c)
                if not '' in rules[c]:
                    break
for nt, first_set in first.items():
    new_set = first_set.copy()
    while any([x[0]=='(' for x in new_set if len(x)>0]):
        old_set = new_set.copy()
        for to_merge in [x for x in new_set if len(x)>0 and x[0]=='(']:
            to_merge = first[to_merge[1]].copy()
            if '' in to_merge:
                to_merge.remove('')
            new_set |= to_merge
        if new_set == old_set:
            break
    new_set = {x for x in new_set if len(x)==0 or x[0]!='('}
    if len(new_set) == 0:
        new_set.add('')
    first[nt] = new_set

print('first set:', first)

# Compute follow set
follow = {nt:set() for nt in rules.keys()}
follow[start].add('$')
for nt, productions in rules.items():
    for prod in productions:
        for i in range(len(prod)):
            if prod[i].isupper():
                if i == len(prod) - 1:
                    follow[prod[i]].add(')'+nt)
                elif prod[i+1].islower():
                    follow[prod[i]].add(prod[i+1])
                else:
                    j = i+1
                    while j < len(prod):
                        if prod[j].islower():
                            follow[prod[i]].add(prod[j])
                            break
                        if prod[j].isupper():
                            follow[prod[i]].add('('+prod[j])
                        if '' not in rules[prod[j]]:
                            break
                        j += 1
follow = {nt:{x for x in fset if x != ')'+nt} for nt, fset in follow.items()}
for nt, follow_set in follow.items():
    new_set = follow_set.copy()
    while any([x[0]==')' for x in new_set if len(x)>0]):
        old_set = new_set.copy()
        for to_merge in [x for x in new_set if len(x)>0 and x[0]==')']:
            to_merge = follow[to_merge[1]].copy()
            new_set |= to_merge
        if new_set == old_set:
            break
    new_set = {x for x in new_set if len(x)==0 or x[0]!=')'}
    while any([x[0]=='(' for x in new_set if len(x)>0]):
        to_merge = [x for x in new_set if len(x)>0 and x[0]=='('][0]
        new_set.remove(to_merge)
        to_merge = first[to_merge[1]].copy()
        if '' in to_merge:
            to_merge.remove('')
        new_set |= to_merge
    follow[nt] = new_set

print('follow set:', follow)

# Compute parsing table
terms = {x for z in rules.values() for y in z for x in y if x.islower()}
terms.add('$')
table = {nt:{t:set() for t in terms} for nt in rules.keys()}
for nt, productions in rules.items():
    for prod in productions:
        if prod == '':
            for x in follow[nt]:
                table[nt][x].add(prod)
        elif prod[0].islower():
            table[nt][prod[0]].add(prod)
        else:
            first_set = set()
            for i in range(len(prod)):
                if prod[i].islower():
                    first_set.add(prod[i])
                    break
                first_set |= first[prod[i]]
                if '' not in rules[prod[i]]:
                    break
                if i == len(prod)-1:
                    first_set.add('$')
            for x in first_set:
                if x != '':
                    table[nt][x].add(prod)


print('table:', table)

# Check LL(1)
is_ll1 = all([len(y)<2 for x in [x.values() for x in table.values()] for y in x])

print('is ll1?', is_ll1)