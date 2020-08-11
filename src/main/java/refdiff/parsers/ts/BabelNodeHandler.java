package refdiff.parsers.ts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import refdiff.core.cst.Parameter;
import refdiff.core.cst.CstNode;
import refdiff.core.cst.Stereotype;

abstract class BabelNodeHandler {
    public abstract String getLocalName(CstNode cstNode, TsValueV8 esprimaNode);

    public boolean isCstNode(TsValueV8 babelAst) {
        return true;
    }

    public abstract String getType(TsValueV8 babelAst);

    public TsValueV8 getMainNode(TsValueV8 babelAst) {
        return babelAst;
    }

    public abstract TsValueV8 getBodyNode(TsValueV8 babelAst);

    public String getSimpleName(CstNode cstNode, TsValueV8 babelAst) {
        return getLocalName(cstNode, babelAst);
    }

    public String getNamespace(CstNode cstNode, TsValueV8 babelAst) {
        return null;
    }

    public abstract Set<Stereotype> getStereotypes(CstNode cstNode, TsValueV8 babelAst);

    public List<Parameter> getParameters(CstNode cstNode, TsValueV8 babelAst) {
        return Collections.emptyList();
    }

    protected List<Parameter> extractParameters(TsValueV8 nodeWithParams) {
        TsValueV8 params = nodeWithParams.get("params");
        if (params.isArray()) {
            List<Parameter> parameters = new ArrayList<>(params.size());
            for (int i = 0; i < params.size(); i++) {
                TsValueV8 param = params.get(i);
                if (param.get("type").asString().equals("Identifier")) {
                    parameters.add(new Parameter(param.get("name").asString()));
                } else {
                    //
                }
            }
            return parameters;
        }
        return Collections.emptyList();
    }

    static final Map<String, BabelNodeHandler> RAST_NODE_HANDLERS = new HashMap<>();

    static {
        RAST_NODE_HANDLERS.put("Program", new BabelNodeHandler() {
            public String getLocalName(CstNode cstNode, TsValueV8 esprimaNode) {
                String filePath = cstNode.getLocation().getFile();
                if (filePath.lastIndexOf('/') != -1) {
                    return filePath.substring(filePath.lastIndexOf('/') + 1);
                } else {
                    return filePath;
                }
            }

            public String getNamespace(CstNode cstNode, TsValueV8 esprimaNode) {
                String filePath = cstNode.getLocation().getFile();
                if (filePath.lastIndexOf('/') != -1) {
                    return filePath.substring(0, filePath.lastIndexOf('/') + 1);
                } else {
                    return "";
                }
            }

            public Set<Stereotype> getStereotypes(CstNode cstNode, TsValueV8 esprimaNode) {
                return Collections.singleton(Stereotype.HAS_BODY);
            }

            @Override
            public TsValueV8 getBodyNode(TsValueV8 esprimaNode) {
                return esprimaNode.get("body");
            }

            @Override
            public String getType(TsValueV8 babelAst) {
                return TsNodeType.FILE;
            }
        });

        RAST_NODE_HANDLERS.put("FunctionDeclaration", new BabelNodeHandler() {
            public String getLocalName(CstNode cstNode, TsValueV8 esprimaNode) {
                return esprimaNode.get("id").get("name").asString();
            }

            public Set<Stereotype> getStereotypes(CstNode cstNode, TsValueV8 esprimaNode) {
                return Collections.singleton(Stereotype.HAS_BODY);
            }

            @Override
            public List<Parameter> getParameters(CstNode cstNode, TsValueV8 esprimaNode) {
                return extractParameters(esprimaNode);
            }

            @Override
            public TsValueV8 getBodyNode(TsValueV8 esprimaNode) {
                return esprimaNode.get("body");
            }

            @Override
            public String getType(TsValueV8 babelAst) {
                return TsNodeType.FUNCTION;
            }
        });

        RAST_NODE_HANDLERS.put("VariableDeclarator", new BabelNodeHandler() {
            @Override
            public boolean isCstNode(TsValueV8 babelAst) {
                if (babelAst.has("init")) {
                    String expressionType = babelAst.get("init").get("type").asString();
                    return "FunctionExpression".equals(expressionType) || "ArrowFunctionExpression".equals(expressionType);
                }
                return false;
            }

            @Override
            public String getType(TsValueV8 babelAst) {
                return TsNodeType.FUNCTION;
            }

            public String getLocalName(CstNode cstNode, TsValueV8 esprimaNode) {
                return esprimaNode.get("id").get("name").asString();
            }

            public Set<Stereotype> getStereotypes(CstNode cstNode, TsValueV8 esprimaNode) {
                return Collections.singleton(Stereotype.HAS_BODY);
            }

            @Override
            public List<Parameter> getParameters(CstNode cstNode, TsValueV8 esprimaNode) {
                return extractParameters(esprimaNode.get("init"));
            }

            @Override
            public TsValueV8 getMainNode(TsValueV8 babelAst) {
                return babelAst.get("init");
            }

            @Override
            public TsValueV8 getBodyNode(TsValueV8 esprimaNode) {
                return esprimaNode.get("init").get("body");
            }
        });

        RAST_NODE_HANDLERS.put("ClassDeclaration", new BabelNodeHandler() {
            public String getLocalName(CstNode cstNode, TsValueV8 esprimaNode) {
                return esprimaNode.get("id").get("name").asString();
            }

            public Set<Stereotype> getStereotypes(CstNode cstNode, TsValueV8 esprimaNode) {
                return Collections.emptySet();
            }

            @Override
            public TsValueV8 getBodyNode(TsValueV8 esprimaNode) {
                return esprimaNode.get("body");
            }

            @Override
            public String getType(TsValueV8 babelAst) {
                return TsNodeType.CLASS;
            }
        });

        RAST_NODE_HANDLERS.put("ClassMethod", new BabelNodeHandler() {
            public String getLocalName(CstNode cstNode, TsValueV8 esprimaNode) {
                return esprimaNode.get("key").get("name").asString();
            }

            public Set<Stereotype> getStereotypes(CstNode cstNode, TsValueV8 esprimaNode) {
                String kind = esprimaNode.get("kind").asString();
                if (kind.equals("method")) {
                    return Collections.singleton(Stereotype.TYPE_MEMBER);
                } else if (kind.equals("constructor")) {
                    return Collections.singleton(Stereotype.TYPE_CONSTRUCTOR);
                } else {
                    return Collections.emptySet();
                }
            }

            @Override
            public List<Parameter> getParameters(CstNode cstNode, TsValueV8 esprimaNode) {
                return extractParameters(esprimaNode);
            }

            @Override
            public TsValueV8 getBodyNode(TsValueV8 esprimaNode) {
                return esprimaNode.get("body");
            }

            @Override
            public String getType(TsValueV8 babelAst) {
                return TsNodeType.FUNCTION;
            }
        });

        RAST_NODE_HANDLERS.put("ObjectProperty", new BabelNodeHandler() {
            @Override
            public boolean isCstNode(TsValueV8 babelAst) {
                String keyNodeType = babelAst.get("key").get("type").asString();
                String valueNodeType = babelAst.get("value").get("type").asString();
                boolean hasIdentifier = "Identifier".equals(keyNodeType);
                boolean hasFunctionExpression = "FunctionExpression".equals(valueNodeType) || "ArrowFunctionExpression".equals(valueNodeType);
                return hasIdentifier && hasFunctionExpression;
            }

            @Override
            public String getType(TsValueV8 babelAst) {
                return TsNodeType.FUNCTION;
            }

            public String getLocalName(CstNode cstNode, TsValueV8 babelAst) {
                return babelAst.get("key").get("name").asString();
            }

            public Set<Stereotype> getStereotypes(CstNode cstNode, TsValueV8 babelAst) {
                return Collections.singleton(Stereotype.HAS_BODY);
            }

            @Override
            public List<Parameter> getParameters(CstNode cstNode, TsValueV8 babelAst) {
                return extractParameters(babelAst.get("value"));
            }

            @Override
            public TsValueV8 getMainNode(TsValueV8 babelAst) {
                return babelAst.get("value");
            }

            @Override
            public TsValueV8 getBodyNode(TsValueV8 babelAst) {
                return babelAst.get("value").get("body");
            }
        });

        RAST_NODE_HANDLERS.put("AssignmentExpression", new BabelNodeHandler() {
            @Override
            public boolean isCstNode(TsValueV8 babelAst) {
                String leftNodeType = babelAst.get("left").get("type").asString();
                String rightNodeType = babelAst.get("right").get("type").asString();
                boolean isIdentifier = "Identifier".equals(leftNodeType);
                boolean isMemberIdentifier = "MemberExpression".equals(leftNodeType) && "Identifier".equals(babelAst.get("left").get("property").get("type").asString());
                boolean hasFunctionExpression = "FunctionExpression".equals(rightNodeType) || "ArrowFunctionExpression".equals(rightNodeType);
                return (isIdentifier || isMemberIdentifier) && hasFunctionExpression;
            }

            @Override
            public String getType(TsValueV8 babelAst) {
                return TsNodeType.FUNCTION;
            }

            public String getLocalName(CstNode cstNode, TsValueV8 babelAst) {
                TsValueV8 leftNode = babelAst.get("left");
                String leftNodetype = leftNode.get("type").asString();
                if ("MemberExpression".equals(leftNodetype)) {
                    return leftNode.get("property").get("name").asString();
                }
                return leftNode.get("name").asString();
            }

            public Set<Stereotype> getStereotypes(CstNode cstNode, TsValueV8 babelAst) {
                return Collections.singleton(Stereotype.HAS_BODY);
            }

            @Override
            public List<Parameter> getParameters(CstNode cstNode, TsValueV8 babelAst) {
                return extractParameters(babelAst.get("right"));
            }

            @Override
            public TsValueV8 getMainNode(TsValueV8 babelAst) {
                return babelAst.get("right");
            }

            @Override
            public TsValueV8 getBodyNode(TsValueV8 babelAst) {
                return babelAst.get("right").get("body");
            }
        });
    }
}