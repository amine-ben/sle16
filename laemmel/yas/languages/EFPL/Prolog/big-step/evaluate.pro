% BEGIN ...
:- module(efplBigstep, []).
% END ...
% Evaluate main expression of program
evaluate((Fs, E), V) :-
  evaluate(Fs, [], E, V).

% A constant evaluates to itself
evaluate(_, _, intconst(I), intval(I)).

% An argument name evaluates to its binding
evaluate(_, X, name(N), V) :-
  member((N, V), X).

% A function name evaluates to itself
evaluate(P, X, name(N), funval(N)) :-
  \+ member((N, _), X),
  member((N, _, _, _), P).

% True condition
evaluate(P, X, if(E0, E1, _), V) :-
  evaluate(P, X, E0, boolval(true)),
  evaluate(P, X, E1, V).

% False condition
evaluate(P, X, if(E0, _, E2), V) :-
  evaluate(P, X, E0, boolval(false)),
  evaluate(P, X, E2, V).
 
% Evaluate operator application
evaluate(P, X, binary(O, E1, E2), V0) :-
  evaluate(P, X, E1, V1),
  evaluate(P, X, E2, V2),
  opVal(O, V1, V2, V0).

% Evaluate function application
evaluate(P, X1, apply(F, Es), V) :-
  evaluate(P, X1, F, funval(N)),
  map(efplBigstep:evaluate(P, X1), Es, Vs),
  member((N, _, Ns, E), P),
  zip(Ns, Vs, X2),
  evaluate(P, X2, E, V).
