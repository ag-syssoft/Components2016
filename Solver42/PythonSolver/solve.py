# Recursive backtracking SUDOKU<gasp> solver
# (c) Jesko Hüttenhain
# Published under YouMayOnlyStareAndMarvelInAwePL
def echo(S):
    for i in range(9):
        print(S[i*9:(i+1)*9])
   
# Compute the indices of all boxes which depend on the box with index i. A box
# depends on another if they cannot contain the same value in a legal sudoku.
def affected(i):
    r,c = divmod(i,9)
    I = set(range(c,81,9)) | set(range(r*9,(r+1)*9))
    c //= 3
    r //= 3
    I |= set(range((r*3+0)*9+c*3,(r*3+0)*9+(c+1)*3))
    I |= set(range((r*3+1)*9+c*3,(r*3+1)*9+(c+1)*3))
    I |= set(range((r*3+2)*9+c*3,(r*3+2)*9+(c+1)*3))
    return I
# This is the recursive solver. It expects as its arguments a partially solved
# sudoku S and a list of sets. For each index i, O[i] contains the options that
# are available for box i. 
def _solve(S,O):
    if 0 not in S: # We have found a solution.
        yield [x for x in S]
        return
    # In the following, we find the index "target" of the box with the least
    # number of options still remaining.
    target, best = 0, 10
    for i in range(81):
        if S[i]==0 and len(O[i]) < best:
            target,best = i,len(O[i])
    # Compute all indices that depend on target.
    A = affected(target)
    A.discard(target)
    for k in O[target]:
        # For each option k, we update the list of options, call _solve 
        # recursively and then restore the original state of the array O.
        I = [ i for i in A if k in O[i] ]
        S[target] = k
        for i in I: O[i].remove(k)
        for solution in _solve(S,O): yield solution
        for i in I: O[i].add(k)
    S[target] = 0
   
# This is a wrapper function for solve which generates the initial list of 
# lists of options for the boxes in S.
def solve(S):
    getoptions = lambda i: set(range(1,10)) \
       - set(S[k] for k in affected(i))
    return _solve(S,[getoptions(i) for i in range(81)])
   
if __name__ == '__main__':
    for solution in solve([
        0, 0, 0, 0, 6, 0, 0, 8, 0,
        0, 2, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 1, 0, 0, 0, 0, 0, 0,
        0, 7, 0, 0, 0, 0, 1, 0, 2,
        5, 0, 0, 0, 3, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 4, 0, 0,
        0, 0, 4, 2, 0, 1, 0, 0, 0,
        3, 0, 0, 7, 0, 0, 6, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 5, 0
    ]): echo(solution)