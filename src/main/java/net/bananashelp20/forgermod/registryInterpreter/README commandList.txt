:::::::::::::::::::::
.willi Manual
:::::::::::::::::::::

*********************
General
*********************
#// declares a comment:
    #//comment//#
    #//comment
    comment//#

! declares a command:
    !COMMAND

*********************
Item Commands
*********************
!ALL --> makes all variations including the item without any upgrade #has to be declared instead of the item variation List
!NO_MODEL --> model isn't created, to make room for custom models #has to be declared instead of the item model method
!ULTRA --> makes item extremely strong #has to be decalred instead of the item properties
!CREATE_TOOLSET |[material]|[properties]| --> makes all tools and the ingot for specific material
!NO_ENCHANTS -->  makes clear that item is not enchantable

*********************
Block Commands
*********************
!NO_MODEL --> model isn't created, to make room for custom models #has to be declared instead of the block model method
!INDESTRUCTIBLE --> makes block basically indestructible #has to be declared instead of the block properties


*********************
Recipe Commands
*********************
!SINGLE_OUTPUT --> declares that block only has a single output
