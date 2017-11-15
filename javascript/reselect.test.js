const sinon = require('sinon');
const assert = require('assert');
const { createSelector } = require('reselect');


describe('reselect', ()=>{

    it('should memoize calls to to a selector', ()=>{
        const state = {
            elems: ['foo', 'bar', 'foo']
        };

        const getFoos = sinon.spy(state => state.elems.filter( f=> f === 'foo'));
        const getNumberOfFoos = createSelector(getFoos, (foos) => foos.length);

        getNumberOfFoos(state); getNumberOfFoos(state); getNumberOfFoos(state);

        assert.equal(getFoos.callCount, 1);
    });

    it('should create a simple memoized selector out of a basic selector', ()=>{
        const state = {
            elems: ['foo', 'bar', 'foo']
        };

        const getElems = sinon.spy(state => state.elems);
        const memoizedGetElems = createSelector(getElems, elems => elems);

        memoizedGetElems(state); memoizedGetElems(state); memoizedGetElems(state); 

        assert(sinon.match.array.deepEquals(memoizedGetElems(state), getElems(state)))
        assert.equal(getElems.callCount, 2);
    });

    it('should memoize calls to to a selector, given both state and props', ()=>{
        const state = {
            elems: ['foo', 'bar', 'foo']
        };

        const getOfType = sinon.spy( (state, props) => state.elems.filter( f=> f === props.type));
        const getNumberOfType = createSelector(getOfType, (elemsOfType) => elemsOfType.length);

        const props = {type: 'foo'};
        getNumberOfType(state, props);
        getNumberOfType(state, props);
        getNumberOfType(state, props);
        
        assert.equal(getOfType.callCount, 1);
    });

    it('only memoizes calls when props are strictly equal', ()=>{
        const state = {
            elems: ['foo', 'bar', 'foo']
        };

        const getOfType = sinon.spy( (state, props) => state.elems.filter( f=> f === props.type));
        const getNumberOfType = createSelector(getOfType, (elemsOfType) => elemsOfType.length);

        getNumberOfType(state, {type: 'foo'});
        getNumberOfType(state, {type: 'foo'});
        getNumberOfType(state, {type: 'foo'});
        
        assert.equal(getOfType.callCount, 3);
    });
});
