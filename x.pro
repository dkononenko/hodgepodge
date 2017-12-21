use_module(library(lists)).

start(Pos, Log) :-
    start2(Pos, [0,0,1,1,1,0,0,
                 0,0,1,1,1,0,0,
                 1,1,1,1,1,1,1,
                 1,1,1,1,1,1,1,
                 1,1,1,1,1,1,1,
                 0,0,1,1,1,0,0,
                 0,0,1,1,1,0,0], [], Log).

start2(Pos, Map, Log, Res) :-
    member(Dir, [ur, ru, rd, dr, dl, ld, lu, ul]),
    move(Dir, Pos, Map, NPos, NMap),
    append(Log, [Dir], Log1),
    start2(NPos, NMap, Log1, Res).

start2(_, [0,0,0,0,0,0,0,
           0,0,0,0,0,0,0,
           0,0,0,0,0,0,0,
           0,0,0,0,0,0,0,
           0,0,0,0,0,0,0,
           0,0,0,0,0,0,0,
           0,0,0,0,0,0,0], Res, Res).

move(Dir, Pos, Map, NPos, NMap) :-
    move(Dir, Pos, NPos),
    NPos >= 0, NPos < 49,
    nth0(NPos, Map, 1),
    change(Map, NPos, 0, NMap).


move(ur, Pos, NPos) :- move(Pos, -2, 1, NPos).
move(ru, Pos, NPos) :- move(Pos, -1, 2, NPos).
move(rd, Pos, NPos) :- move(Pos, 1, 2, NPos).
move(dr, Pos, NPos) :- move(Pos, 2, 1, NPos).
move(dl, Pos, NPos) :- move(Pos, 2, -1, NPos).
move(ld, Pos, NPos) :- move(Pos, 1, -2, NPos).
move(lu, Pos, NPos) :- move(Pos, -1, -2, NPos).
move(ul, Pos, NPos) :- move(Pos, -2, -1, NPos).

move(Pos, R, C, NPos) :-
    A is Pos div 7,
    B is (Pos + C) div 7,
    A = B,
    NPos is Pos + R * 7 + C.    

change(List, Pos, Value, NList) :-
    length(A, Pos),
    append(A, [_|B], List),
    append(A, [Value], NList1),
    append(NList1, B, NList).
