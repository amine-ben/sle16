-- BEGIN ...
module Language.ML.Sample where
import Language.ML.Syntax
-- END ...
sample = [Const 14,Store 0,Const 4,Store 1,Const 0,Store 2,Load 0,Store 3,Load 3,Load 1,Geq,Not,CJump 22,Load 3,Load 1,Sub,Store 3,Load 2,Const 1,Add,Store 2,Jump 8]
